package sign;

import luna.ClientResources;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.Transmitter;
import javax.sound.midi.Track;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Arrays;
import java.util.function.LongConsumer;

/**
 * Plays cache MIDI files using one reusable, explicitly connected MIDI device pair.
 * Device operations run on a daemon thread and pending requests replace older requests.
 *
 * @author lare96
 */
public final class MidiPlayer implements AutoCloseable {

	static final int MAX_MIDI_BYTES = 2 * 1024 * 1024;
	private static final long FADE_MILLIS = 1000;

	private final Object requests = new Object();
	private final DeviceFactory factory;
	private final File soundbankFile;
	private final Thread worker;
	private Request pending;
	private long generation;
	private boolean closed;
	private boolean volumeChanged;
	private int volume;
	// Only the music worker owns these devices.
	private Sequencer sequencer;
	private Synthesizer synthesizer;
	private Transmitter transmitter;
	private VolumeReceiver receiver;
	private double fadeGain = 1.0;

	public MidiPlayer() {
		this(new DeviceFactory(), new File("data/soundbank.sf2"));
	}

	MidiPlayer(DeviceFactory factory, File soundbankFile) {
		this.factory = factory;
		this.soundbankFile = soundbankFile;
		worker = new Thread(this::run, "luna-midi");
		worker.setDaemon(true);
		worker.start();
	}

	public void play(byte[] data, boolean loop) {
		play(data, loop, null);
	}

	/**
	 * Queues MIDI or RIFF/RMID data. The optional callback runs on the music thread,
	 * reporting the track duration in milliseconds after starting, or -1 on failure.
	 * Callers must disregard callbacks belonging to their own superseded requests.
	 */
	public void play(byte[] data, boolean loop, LongConsumer started) {
		synchronized (requests) {
			if (!closed) {
				// Reject malformed/oversized requests on the worker, before cloning or
				// allocating based on untrusted cache data. Null here is not a stop.
				byte[] copy = data == null || data.length > MAX_MIDI_BYTES ? new byte[0] : data.clone();
				pending = new Request(++generation, copy, loop, started, false);
				requests.notifyAll();
			}
		}
	}

	public void stop() {
		synchronized (requests) {
			if (!closed) {
				pending = new Request(++generation, null, false, null, false);
				requests.notifyAll();
			}
		}
	}

	/**
	 * Cancels a pending region transition while retaining the current song during loading.
	 */
	public void prepareTransition() {
		synchronized (requests) {
			if (!closed) {
				pending = new Request(++generation, null, false, null, true);
				requests.notifyAll();
			}
		}
	}

	/**
	 * Sets master attenuation in hundredths of a decibel, preserving track volume
	 * and expression. Zero is full volume; the client's other levels are -400,
	 * -800, and -1200.
	 */
	public void setVolume(int hundredthsDb) {
		synchronized (requests) {
			if (!closed) {
				volume = Math.max(-9600, Math.min(0, hundredthsDb));
				volumeChanged = true;
				requests.notifyAll();
			}
		}
	}

	@Override
	public void close() {
		synchronized (requests) {
			closed = true;
			generation++;
			pending = null;
			requests.notifyAll();
		}
		// Shutdown never waits for a MIDI provider on the game thread. The daemon
		// releases its devices in finally, including any partially opened pair.
	}

	private void run() {
		try {
			while (true) {
				Request request;
				int nextVolume;
				boolean applyVolume;
				synchronized (requests) {
					while (!closed && pending == null && !volumeChanged)
						requests.wait();
					if (closed)
						return;
					request = pending;
					pending = null;
					nextVolume = volume;
					applyVolume = volumeChanged;
					volumeChanged = false;
				}
				try {
					if (receiver != null && applyVolume)
						receiver.setVolume(nextVolume);
					if (request != null)
						process(request);
				} catch (Exception exception) {
					closeDevices();
					System.err.println("Unable to play MIDI: " + exception);
					if (request != null && isCurrent(request))
						notifyStarted(request, -1);
				}
			}
		} catch (InterruptedException exception) {
			Thread.currentThread().interrupt();
		} finally {
			closeDevices();
		}
	}

	private void process(Request request) throws Exception {
		if (!isCurrent(request))
			return;
		if (request.preserve) {
			if (receiver != null)
				fade(request, 1.0);
			return;
		}
		if (request.data == null) {
			stopTrack();
			return;
		}
		if (request.data.length == 0)
			throw new InvalidMidiDataException("Missing, empty, or oversized MIDI data (maximum 2 MiB)");
		byte[] midi = unwrapRmid(request.data);
		validateSmf(midi);
		Sequence sequence = factory.read(midi);
		if (!isCurrent(request))
			return;
		openDevices();
		if (!isCurrent(request))
			return;
		if (request.loop && sequencer.isRunning() && !fade(request, 0.0))
			return;
		fadeGain = request.loop ? 0.0 : 1.0;
		receiver.setFade(fadeGain);
		stopTrack();
		if (!isCurrent(request))
			return;
		int currentVolume;
		synchronized (requests) {
			currentVolume = volume;
		}
		receiver.setVolume(currentVolume);
		sequencer.setSequence(sequence);
		sequencer.setTickPosition(0);
		// Clearing a Java Sound sequence caches its old tempo and can suppress the
		// next song's tick-zero tempo event. Restore the new song's authored tempo.
		sequencer.setTempoFactor(1.0f);
		sequencer.setTempoInMPQ(initialTempo(sequence));
		sequencer.setLoopStartPoint(0);
		sequencer.setLoopEndPoint(-1);
		sequencer.setLoopCount(request.loop ? Sequencer.LOOP_CONTINUOUSLY : 0);
		synchronized (requests) {
			// A stop/replacement submitted during parsing or device opening must
			// prevent this track from starting.
			if (closed || request.generation != generation)
				return;
			sequencer.start();
		}
		if (isCurrent(request))
			notifyStarted(request, sequence.getMicrosecondLength() / 1000L);
		if (request.loop)
			fade(request, 1.0);
	}

	/**
	 * Uses the standard default when the conductor track has no initial tempo event.
	 */
	static int initialTempo(Sequence sequence) {
		int tempo = 500000;
		Track[] tracks = sequence.getTracks();
		if (tracks.length != 0) {
			Track conductor = tracks[0];
			for (int index = 0; index < conductor.size() && conductor.get(index).getTick() == 0; index++) {
				MidiMessage message = conductor.get(index).getMessage();
				if (message instanceof MetaMessage && ((MetaMessage) message).getType() == 81) {
					byte[] data = ((MetaMessage) message).getData();
					if (data.length == 3) {
						int value = ((data[0] & 255) << 16) | ((data[1] & 255) << 8) | (data[2] & 255);
						if (value > 0)
							tempo = value;
					}
				}
			}
		}
		return tempo;
	}

	/**
	 * Runs a cancellable envelope on the audio worker, using elapsed wall time.
	 */
	private boolean fade(Request request, double target) throws InterruptedException {
		double from = fadeGain;
		long start = System.nanoTime();
		while (true) {
			synchronized (requests) {
				if (closed || generation != request.generation)
					return false;
				if (volumeChanged) {
					receiver.setVolume(volume);
					volumeChanged = false;
				}
				double progress = Math.min(1.0, (System.nanoTime() - start) / (FADE_MILLIS * 1000000.0));
				fadeGain = from + (target - from) * progress;
				receiver.setFade(fadeGain);
				if (progress >= 1.0 || from == target)
					return true;
				requests.wait(20);
			}
		}
	}

	private boolean isCurrent(Request request) {
		synchronized (requests) {
			return !closed && request.generation == generation;
		}
	}

	private void notifyStarted(Request request, long duration) {
		if (request.started != null) {
			try {
				request.started.accept(duration);
			} catch (RuntimeException exception) {
				System.err.println("MIDI start callback failed: " + exception);
			}
		}
	}

	private void openDevices() throws Exception {
		if (sequencer != null)
			return;
		synthesizer = factory.synthesizer();
		synthesizer.open();
		if (soundbankFile != null) {
			Soundbank bank;
			if (soundbankFile.isFile()) {
				bank = MidiSystem.getSoundbank(soundbankFile);
			} else {
				try (InputStream stream = ClientResources.openBundled("data/soundbank.sf2")) {
					bank = MidiSystem.getSoundbank(stream);
				}
			}
			if (bank == null || !synthesizer.isSoundbankSupported(bank)
					|| !synthesizer.loadAllInstruments(bank))
				throw new InvalidMidiDataException("Unsupported soundbank: " + soundbankFile);
		}
		receiver = new VolumeReceiver(synthesizer.getReceiver());
		sequencer = factory.sequencer();
		sequencer.open();
		transmitter = sequencer.getTransmitter();
		transmitter.setReceiver(receiver);
	}

	private void stopTrack() throws InvalidMidiDataException {
		if (sequencer != null) {
			// stop may wait for the playback thread, which may be in Receiver.send.
			// Never hold the receiver monitor while stopping/closing a sequencer.
			sequencer.stop();
			sequencer.setSequence((Sequence) null);
		}
		if (receiver != null)
			receiver.reset();
	}

	private void closeDevices() {
		if (sequencer != null)
			quietly(sequencer::stop);
		if (receiver != null)
			quietly(receiver::reset);
		if (transmitter != null)
			quietly(transmitter::close);
		if (sequencer != null)
			quietly(sequencer::close);
		if (receiver != null)
			quietly(receiver::close);
		if (synthesizer != null)
			quietly(synthesizer::close);
		transmitter = null;
		sequencer = null;
		receiver = null;
		synthesizer = null;
	}

	private static void quietly(Runnable operation) {
		try {
			operation.run();
		} catch (RuntimeException exception) {
			System.err.println("Unable to close MIDI resource: " + exception);
		}
	}

	/**
	 * Unwraps the standard MIDI data chunk in a Windows RIFF/RMID container.
	 */
	static byte[] unwrapRmid(byte[] data) throws InvalidMidiDataException {
		if (!matches(data, 0, "RIFF"))
			return data;
		if (data.length < 12 || !matches(data, 8, "RMID"))
			throw new InvalidMidiDataException("Invalid RIFF/RMID header");
		long end = 8L + littleEndianInt(data, 4);
		if (end < 12 || end > data.length)
			throw new InvalidMidiDataException("Truncated RIFF/RMID container");
		for (long offset = 12; offset + 8 <= end;) {
			int start = (int) offset;
			long length = littleEndianInt(data, start + 4);
			long next = offset + 8L + length;
			if (next > end)
				throw new InvalidMidiDataException("Truncated RIFF/RMID chunk");
			if (matches(data, start, "data"))
				return Arrays.copyOfRange(data, start + 8, (int) next);
			offset = next + (length & 1);
		}
		throw new InvalidMidiDataException("RIFF/RMID has no MIDI data chunk");
	}

	private static boolean matches(byte[] data, int offset, String text) {
		if (offset + text.length() > data.length)
			return false;
		for (int index = 0; index < text.length(); index++)
			if (data[offset + index] != text.charAt(index))
				return false;
		return true;
	}

	private static long littleEndianInt(byte[] data, int offset) {
		return (data[offset] & 255L) | ((data[offset + 1] & 255L) << 8)
				| ((data[offset + 2] & 255L) << 16) | ((data[offset + 3] & 255L) << 24);
	}

	/**
	 * Checks chunk lengths before Java Sound allocates its track buffers. A small
	 * malformed file can otherwise declare an arbitrarily large MTrk allocation.
	 */
	static void validateSmf(byte[] data) throws InvalidMidiDataException {
		if (data.length < 14 || !matches(data, 0, "MThd"))
			throw new InvalidMidiDataException("Missing MIDI header");
		long headerLength = bigEndianInt(data, 4);
		if (headerLength < 6 || headerLength > data.length - 8L)
			throw new InvalidMidiDataException("Invalid MIDI header length");
		int format = ((data[8] & 255) << 8) | (data[9] & 255);
		int tracks = ((data[10] & 255) << 8) | (data[11] & 255);
		// Java Sound reads the track count as a signed short and supports types 0/1.
		if (format > 1 || tracks < 1 || tracks > 32767 || (format == 0 && tracks != 1))
			throw new InvalidMidiDataException("Unsupported MIDI format or track count");
		long offset = 8L + headerLength;
		for (int found = 0; found < tracks;) {
			if (offset + 8L > data.length)
				throw new InvalidMidiDataException("Missing MIDI track chunk");
			int start = (int) offset;
			long length = bigEndianInt(data, start + 4);
			if (length > data.length - offset - 8L)
				throw new InvalidMidiDataException("Truncated or oversized MIDI track chunk");
			if (matches(data, start, "MTrk"))
				found++;
			offset += 8L + length;
		}
	}

	private static long bigEndianInt(byte[] data, int offset) {
		return ((data[offset] & 255L) << 24) | ((data[offset + 1] & 255L) << 16)
				| ((data[offset + 2] & 255L) << 8) | (data[offset + 3] & 255L);
	}

	/**
	 * Supplies Java Sound devices; package access allows tests without audio hardware.
	 *
	 * @author lare96
	 */
	static class DeviceFactory {
		Sequencer sequencer() throws Exception {
			return MidiSystem.getSequencer(false);
		}

		Synthesizer synthesizer() throws Exception {
			return MidiSystem.getSynthesizer();
		}

		Sequence read(byte[] data) throws Exception {
			return MidiSystem.getSequence(new ByteArrayInputStream(data));
		}
	}

	/**
	 * A single replaceable play or stop command.
	 *
	 * @author lare96
	 */
	private static final class Request {
		final long generation;
		final byte[] data;
		final boolean loop;
		final LongConsumer started;
		final boolean preserve;

		Request(long generation, byte[] data, boolean loop, LongConsumer started, boolean preserve) {
			this.generation = generation;
			this.data = data;
			this.loop = loop;
			this.started = started;
			this.preserve = preserve;
		}
	}

	/**
	 * Applies master gain to authored 14-bit channel volume without altering expression.
	 * Serializes sequencer messages and volume updates before forwarding to the synth.
	 *
	 * @author lare96
	 */
	static final class VolumeReceiver implements Receiver {
		private final Receiver output;
		private final int[] channelVolume = new int[16];
		private double gain = 1.0;
		private double fade = 1.0;
		private boolean closed;

		VolumeReceiver(Receiver output) {
			this.output = output;
			Arrays.fill(channelVolume, 100 << 7);
		}

		synchronized void setVolume(int hundredthsDb) {
			gain = Math.pow(10.0, hundredthsDb / 2000.0);
			if (!closed)
				for (int channel = 0; channel < 16; channel++)
					sendVolume(channel, -1);
		}

		synchronized void setFade(double value) {
			fade = Math.max(0.0, Math.min(1.0, value));
			if (!closed)
				for (int channel = 0; channel < 16; channel++)
					sendVolume(channel, -1);
		}

		synchronized void reset() {
			if (closed)
				return;
			Arrays.fill(channelVolume, 100 << 7);
			for (int channel = 0; channel < 16; channel++) {
				control(channel, 64, 0, -1);
				control(channel, 123, 0, -1);
				control(channel, 120, 0, -1);
				control(channel, 121, 0, -1);
				control(channel, 0, 0, -1);
				control(channel, 32, 0, -1);
				message(ShortMessage.PROGRAM_CHANGE, channel, 0, 0, -1);
				message(ShortMessage.PITCH_BEND, channel, 0, 64, -1);
				control(channel, 11, 127, -1);
				control(channel, 10, 64, -1);
				sendVolume(channel, -1);
			}
		}

		@Override
		public synchronized void send(MidiMessage message, long timestamp) {
			if (closed)
				return;
			if (message instanceof ShortMessage) {
				ShortMessage shortMessage = (ShortMessage) message;
				int channel = shortMessage.getChannel();
				if (shortMessage.getCommand() == ShortMessage.CONTROL_CHANGE) {
					int controller = shortMessage.getData1();
					if (controller == 7 || controller == 39) {
						channelVolume[channel] = controller == 7
								? (shortMessage.getData2() << 7) | (channelVolume[channel] & 127)
								: (channelVolume[channel] & 16256) | shortMessage.getData2();
						sendVolume(channel, timestamp);
						return;
					}
					if (controller == 121) {
						output.send(message, timestamp);
						// CC121 does not change the authored CC7 volume, but providers
						// can reset their gain; restore the attenuated channel level.
						sendVolume(channel, timestamp);
						return;
					}
				}
			}
			output.send(message, timestamp);
			if (message.getStatus() != 0xf0)
				return;
			byte[] bytes = message.getMessage();
			if (bytes.length >= 6
					&& (bytes[1] & 255) == 0x7e && bytes[3] == 9
					&& (bytes[4] == 1 || bytes[4] == 3)) {
				// General MIDI resets can restore synth channel volumes mid-track.
				Arrays.fill(channelVolume, 100 << 7);
				for (int channel = 0; channel < 16; channel++)
					sendVolume(channel, timestamp);
			}
		}

		private void sendVolume(int channel, long timestamp) {
			int scaled = (int) Math.round(channelVolume[channel] * gain * fade);
			control(channel, 7, scaled >> 7, timestamp);
			control(channel, 39, scaled & 127, timestamp);
		}

		private void control(int channel, int controller, int value, long timestamp) {
			message(ShortMessage.CONTROL_CHANGE, channel, controller, value, timestamp);
		}

		private void message(int command, int channel, int first, int second, long timestamp) {
			try {
				output.send(new ShortMessage(command, channel, first, second), timestamp);
			} catch (InvalidMidiDataException exception) {
				throw new IllegalArgumentException(exception);
			}
		}

		@Override
		public synchronized void close() {
			if (!closed) {
				closed = true;
				output.close();
			}
		}
	}
}
