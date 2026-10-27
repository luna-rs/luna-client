package sign;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Synthesizer;
import javax.sound.midi.SysexMessage;
import javax.sound.midi.Track;
import javax.sound.midi.Transmitter;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BooleanSupplier;

/**
 * Deterministic MIDI lifecycle and volume regression tests without audio hardware.
 * Run with java sign.MidiPlayerTest after compiling this file and MidiPlayer.java.
 *
 * @author lare96
 */
public final class MidiPlayerTest {

	public static void main(String[] args) throws Exception {
		byte[] midi = midi();
		testRmid(midi);
		testVolume();
		testFades(midi);
		testRealSequencerTempo();
		testTransitions(midi);
		testLatestRequest(midi);
		testStopDuringParse(midi);
		testCloseDuringParse(midi);
		testInvalidRecovery(midi);
		testInputLimit(midi);
		testChunkLimits(midi);
		for (int failure = 1; failure <= 4; failure++)
			testPartialFailure(midi, failure);
		System.out.println("MidiPlayerTest passed: RMID, volume, 1000 track lifecycles, "
				+ "10000 coalesced requests, stop/close cancellation, invalid MIDI, partial failures.");
	}

	private static void testFades(byte[] midi) throws Exception {
		Factory factory = new Factory();
		MidiPlayer player = new MidiPlayer(factory, null);
		AtomicLong result = new AtomicLong(-2);
		try {
			player.play(midi, false, result::set);
			await(() -> result.get() == 500);
			Sequence old = factory.sequence;
			int full = factory.output.volume(0);
			result.set(-2);
			player.play(midi, true, result::set);
			await(() -> factory.output.volume(0) < full * 3 / 4);
			check(factory.sequence == old && result.get() == -2, "old track plays during fade-out");
			player.setVolume(-400);
			await(() -> result.get() == 500);
			check(factory.output.volume(0) < full / 2, "new track begins quietly");
			int attenuated = (int) Math.round(full * Math.pow(10.0, -0.2));
			await(() -> factory.output.volume(0) == attenuated);
			check(factory.starts.get() == 2, "fade-in completes at user's selected volume");
			player.play(midi, true);
			await(() -> factory.output.volume(0) < attenuated * 3 / 4);
			player.prepareTransition();
			await(() -> factory.output.volume(0) == attenuated);
			check(factory.starts.get() == 2, "new region invalidates a pending transition");
			player.play(midi, true);
			await(() -> factory.output.volume(0) < attenuated * 3 / 4);
			player.stop();
			await(() -> factory.sequence == null);
			check(factory.starts.get() == 2, "stop during fade never starts replacement");
		} finally {
			player.close();
		}
		await(factory::allClosed);
	}

	private static void testRealSequencerTempo() throws Exception {
		// Uses the actual JDK sequencer, with no sound card or synthesizer required.
		Sequencer sequencer = MidiSystem.getSequencer(false);
		Factory factory = new Factory() {
			@Override
			Sequencer sequencer() {
				return sequencer;
			}
		};
		MidiPlayer player = new MidiPlayer(factory, null);
		AtomicLong duration = new AtomicLong(-2);
		try {
			for (int tempo : new int[]{444444, 600000, 500000}) {
				Sequence sequence = new Sequence(Sequence.PPQ, 480);
				Track track = sequence.createTrack();
				if (tempo != 500000)
					track.add(new MidiEvent(tempo(tempo), 0));
				track.add(new MidiEvent(tempo(750000), 240));
				track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON, 0, 60, 100), 4800));
				ByteArrayOutputStream bytes = new ByteArrayOutputStream();
				MidiSystem.write(sequence, 1, bytes);
				duration.set(-2);
				player.play(bytes.toByteArray(), false, duration::set);
				await(() -> duration.get() >= 0);
				check(Math.abs(sequencer.getTempoInMPQ() - tempo) < 1,
						"each new song restores its own initial tempo, including default 120 BPM");
				check(sequencer.getTempoFactor() == 1.0f, "normal playback speed");
				await(() -> sequencer.getTempoInMPQ() == 750000);
				check(sequencer.getTickPosition() >= 240, "later authored tempo changes still apply");
				sequencer.setTempoFactor(1.5f);
			}
		} finally {
			player.close();
		}
		await(() -> !sequencer.isOpen());
	}

	private static MetaMessage tempo(int mpq) throws Exception {
		MetaMessage message = new MetaMessage();
		message.setMessage(81, new byte[]{(byte) (mpq >> 16), (byte) (mpq >> 8), (byte) mpq}, 3);
		return message;
	}

	private static void testTransitions(byte[] midi) throws Exception {
		Factory factory = new Factory();
		MidiPlayer player = new MidiPlayer(factory, null);
		AtomicLong result = new AtomicLong(-2);
		try {
			for (int index = 0; index < 1000; index++) {
				result.set(-2);
				boolean loop = index % 2 == 0;
				player.play(midi, loop, result::set);
				await(() -> result.get() >= 0);
				check(result.get() == 500, "callback reports MIDI duration");
				check(factory.loopCount == (loop ? Sequencer.LOOP_CONTINUOUSLY : 0), "loop mode");
				player.stop();
				await(() -> factory.sequence == null);
			}
			check(factory.synths.get() == 1 && factory.sequencers.get() == 1, "reuse devices");
			check(factory.starts.get() == 1000, "all sequential tracks started");
			check(factory.output.control(0, 64) == 0, "stop releases sustain");
			check(factory.output.control(0, 120) == 0, "stop silences all sound");
			check(factory.output.control(0, 123) == 0, "stop releases all notes");
		} finally {
			player.close();
		}
		await(factory::allClosed);
		player.play(midi, true);
		player.stop();
		player.setVolume(-400);
		player.close();
		check(factory.synths.get() == 1 && factory.sequencers.get() == 1, "close is terminal");
	}

	private static void testLatestRequest(byte[] midi) throws Exception {
		Factory factory = new Factory();
		factory.blockRead();
		MidiPlayer player = new MidiPlayer(factory, null);
		AtomicLong result = new AtomicLong(-2);
		try {
			player.play(midi, true, ignored -> {
				throw new AssertionError("Superseded callback");
			});
			check(factory.entered.await(5, TimeUnit.SECONDS), "parse began");
			for (int index = 0; index < 10000; index++) {
				player.play(midi, true);
				player.setVolume(-400 * (index % 4));
			}
			player.play(midi, false, result::set);
			factory.release.countDown();
			await(() -> result.get() == 500);
			check(factory.reads.get() == 2, "only latest pending request parsed");
			check(factory.starts.get() == 1, "only latest track starts");
			check(factory.synths.get() == 1 && factory.sequencers.get() == 1, "bounded devices");
		} finally {
			factory.release.countDown();
			player.close();
		}
		await(factory::allClosed);
	}

	private static void testStopDuringParse(byte[] midi) throws Exception {
		Factory factory = new Factory();
		factory.blockRead();
		MidiPlayer player = new MidiPlayer(factory, null);
		try {
			player.play(midi, true);
			check(factory.entered.await(5, TimeUnit.SECONDS), "parse began");
			player.stop();
			factory.release.countDown();
			await(() -> factory.finishedReads.get() == 1);
			// The subsequent callback is a barrier for processing the preceding stop.
			AtomicLong result = new AtomicLong(-2);
			player.play(midi, false, result::set);
			await(() -> result.get() == 500);
			check(factory.starts.get() == 1, "stop cancels parsing request");
		} finally {
			factory.release.countDown();
			player.close();
		}
		await(factory::allClosed);
	}

	private static void testCloseDuringParse(byte[] midi) throws Exception {
		Factory factory = new Factory();
		factory.blockRead();
		MidiPlayer player = new MidiPlayer(factory, null);
		player.play(midi, true);
		check(factory.entered.await(5, TimeUnit.SECONDS), "parse began");
		player.close();
		factory.release.countDown();
		await(() -> factory.finishedReads.get() == 1);
		check(factory.synths.get() == 0 && factory.sequencers.get() == 0, "close prevents device opening");
	}

	private static void testInvalidRecovery(byte[] midi) throws Exception {
		Factory factory = new Factory();
		MidiPlayer player = new MidiPlayer(factory, null);
		AtomicLong result = new AtomicLong(-2);
		try {
			player.play(midi, true, result::set);
			await(() -> result.get() == 500);
			result.set(-2);
			player.play(new byte[] { 1, 2, 3 }, true, result::set);
			await(() -> result.get() == -1);
			check(factory.allClosed(), "invalid input releases active devices");
			result.set(-2);
			player.play(midi, false, result::set);
			await(() -> result.get() == 500);
			check(factory.starts.get() == 2, "valid input recovers after malformed MIDI");
		} finally {
			player.close();
		}
		await(factory::allClosed);
	}

	private static void testPartialFailure(byte[] midi, int failure) throws Exception {
		Factory factory = new Factory();
		factory.failAt = failure;
		MidiPlayer player = new MidiPlayer(factory, null);
		AtomicLong result = new AtomicLong(-2);
		try {
			player.play(midi, true, result::set);
			await(() -> result.get() == -1);
			check(factory.allClosed(), "partial init closes every acquired resource: " + failure);
			factory.failAt = 0;
			result.set(-2);
			player.play(midi, true, result::set);
			await(() -> result.get() == 500);
		} finally {
			player.close();
		}
		await(factory::allClosed);
	}

	private static void testInputLimit(byte[] midi) throws Exception {
		Factory factory = new Factory();
		MidiPlayer player = new MidiPlayer(factory, null);
		AtomicLong result = new AtomicLong(-2);
		try {
			player.play(new byte[MidiPlayer.MAX_MIDI_BYTES + 1], false, result::set);
			await(() -> result.get() == -1);
			check(factory.reads.get() == 0, "oversized input rejected before parsing");
			result.set(-2);
			player.play(null, false, result::set);
			await(() -> result.get() == -1);
			check(factory.reads.get() == 0, "null input rejected before parsing");
			result.set(-2);
			player.play(midi, false, result::set);
			await(() -> result.get() == 500);
		} finally {
			player.close();
		}
		await(factory::allClosed);
	}

	private static void testChunkLimits(byte[] midi) throws Exception {
		byte[] hugeTrack = { 'M', 'T', 'h', 'd', 0, 0, 0, 6, 0, 0, 0, 1, 1, (byte) 0xe0,
				'M', 'T', 'r', 'k', 0x40, 0, 0, 0 };
		byte[] hugeHeader = hugeTrack.clone();
		hugeHeader[4] = 0x40;
		byte[] shortHeader = hugeTrack.clone();
		shortHeader[7] = 5;
		byte[] missingTrack = Arrays.copyOf(midi, 14);
		byte[] unsignedLength = hugeTrack.clone();
		unsignedLength[18] = (byte) 0xff;
		Factory factory = new Factory();
		MidiPlayer player = new MidiPlayer(factory, null);
		AtomicLong result = new AtomicLong(-2);
		try {
			for (byte[] malformed : new byte[][] { hugeTrack, hugeHeader, shortHeader, missingTrack,
					unsignedLength, Arrays.copyOf(midi, midi.length - 1) }) {
				result.set(-2);
				player.play(malformed, false, result::set);
				await(() -> result.get() == -1);
				check(factory.reads.get() == 0, "malformed chunk rejected before Java Sound allocation");
			}
			result.set(-2);
			player.play(midi, false, result::set);
			await(() -> result.get() == 500);
			check(factory.reads.get() == 1, "valid MIDI still reaches Java Sound");
		} finally {
			player.close();
		}
		await(factory::allClosed);
	}

	private static void testVolume() throws Exception {
		Output output = new Output();
		MidiPlayer.VolumeReceiver receiver = new MidiPlayer.VolumeReceiver(output);
		for (int volume : new int[] { 0, -400, -800, -1200 }) {
			receiver.setVolume(volume);
			receiver.send(new ShortMessage(ShortMessage.CONTROL_CHANGE, 3, 7, 80), 42);
			receiver.send(new ShortMessage(ShortMessage.CONTROL_CHANGE, 3, 39, 23), 42);
			int expected = (int) Math.round(((80 << 7) | 23) * Math.pow(10, volume / 2000.0));
			check(output.volume(3) == expected, "14-bit volume at " + volume);
			receiver.send(new ShortMessage(ShortMessage.CONTROL_CHANGE, 3, 11, 45), 42);
			check(output.control(3, 11) == 45, "expression retained");
			receiver.send(new ShortMessage(ShortMessage.CONTROL_CHANGE, 3, 121, 0), 42);
			check(output.volume(3) == expected, "CC121 preserves authored attenuated volume");
		}
		receiver.reset();
		check(output.volume(3) == (int) Math.round((100 << 7) * Math.pow(10, -0.6)), "track reset volume");
		check(output.control(3, 11) == 127 && output.control(3, 10) == 64, "track reset expression and pan");
		check(output.control(3, 0) == 0 && output.control(3, 32) == 0, "track reset bank");
		check(output.program[3] == 0 && output.pitch[3] == 8192, "track reset program and pitch");
		receiver.send(new ShortMessage(ShortMessage.CONTROL_CHANGE, 3, 7, 127), -1);
		receiver.send(new SysexMessage(new byte[] { (byte) 0xf0, 0x7e, 0x7f, 9, 1, (byte) 0xf7 }, 6), -1);
		check(output.volume(3) == (int) Math.round((100 << 7) * Math.pow(10, -0.6)), "GM reset retains master gain");
		receiver.close();
		receiver.close();
		check(output.closed.get() == 1, "receiver closes once");
	}

	private static void testRmid(byte[] midi) throws Exception {
		ByteArrayOutputStream body = new ByteArrayOutputStream();
		body.write("RMID".getBytes("US-ASCII"));
		body.write("JUNK".getBytes("US-ASCII"));
		littleEndian(body, 1);
		body.write(7);
		body.write(0);
		body.write("data".getBytes("US-ASCII"));
		littleEndian(body, midi.length);
		body.write(midi);
		if ((midi.length & 1) != 0)
			body.write(0);
		ByteArrayOutputStream file = new ByteArrayOutputStream();
		file.write("RIFF".getBytes("US-ASCII"));
		littleEndian(file, body.size());
		file.write(body.toByteArray());
		byte[] rmid = file.toByteArray();
		check(Arrays.equals(MidiPlayer.unwrapRmid(rmid), midi), "RMID data extraction with odd padded chunk");
		check(MidiPlayer.unwrapRmid(midi) == midi, "normal MIDI passes through");
		expectInvalid(Arrays.copyOf(rmid, rmid.length - 1));
		rmid[26] = (byte) 255;
		rmid[27] = (byte) 255;
		rmid[28] = (byte) 255;
		rmid[29] = (byte) 255;
		expectInvalid(rmid);
	}

	private static void expectInvalid(byte[] data) throws Exception {
		try {
			MidiPlayer.unwrapRmid(data);
			throw new AssertionError("Malformed RMID accepted");
		} catch (InvalidMidiDataException expected) {
			// Expected bounded rejection, without allocation from the declared size.
		}
	}

	private static void littleEndian(ByteArrayOutputStream output, int value) {
		for (int shift = 0; shift < 32; shift += 8)
			output.write(value >>> shift);
	}

	private static byte[] midi() throws Exception {
		Sequence sequence = new Sequence(Sequence.PPQ, 480);
		Track track = sequence.createTrack();
		track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_ON, 0, 60, 100), 0));
		track.add(new MidiEvent(new ShortMessage(ShortMessage.NOTE_OFF, 0, 60, 0), 480));
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		MidiSystem.write(sequence, 1, output);
		return output.toByteArray();
	}

	private static void await(BooleanSupplier condition) throws Exception {
		long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
		while (!condition.getAsBoolean()) {
			if (System.nanoTime() > deadline)
				throw new AssertionError("Timed out waiting for MIDI worker");
			Thread.sleep(1);
		}
	}

	private static void check(boolean condition, String message) {
		if (!condition)
			throw new AssertionError(message);
	}

	/**
	 * Observable fake devices keep lifecycle tests independent of platform providers.
	 *
	 * @author lare96
	 */
	private static class Factory extends MidiPlayer.DeviceFactory {
		final AtomicInteger synths = new AtomicInteger();
		final AtomicInteger sequencers = new AtomicInteger();
		final AtomicInteger synthClosed = new AtomicInteger();
		final AtomicInteger sequencerClosed = new AtomicInteger();
		final AtomicInteger receivers = new AtomicInteger();
		final AtomicInteger receiverClosed = new AtomicInteger();
		final AtomicInteger transmitters = new AtomicInteger();
		final AtomicInteger transmitterClosed = new AtomicInteger();
		final AtomicInteger starts = new AtomicInteger();
		final AtomicInteger reads = new AtomicInteger();
		final AtomicInteger finishedReads = new AtomicInteger();
		final Output output = new Output();
		volatile Sequence sequence;
		volatile int loopCount;
		volatile int failAt;
		volatile boolean block;
		volatile boolean running;
		CountDownLatch entered;
		CountDownLatch release;

		void blockRead() {
			block = true;
			entered = new CountDownLatch(1);
			release = new CountDownLatch(1);
		}

		@Override
		Sequence read(byte[] data) throws Exception {
			reads.incrementAndGet();
			if (block) {
				block = false;
				entered.countDown();
				check(release.await(5, TimeUnit.SECONDS), "release blocked parser");
			}
			try {
				return super.read(data);
			} finally {
				finishedReads.incrementAndGet();
			}
		}

		@Override
		Synthesizer synthesizer() {
			synths.incrementAndGet();
			return (Synthesizer) Proxy.newProxyInstance(getClass().getClassLoader(),
					new Class<?>[] { Synthesizer.class }, (proxy, method, args) -> {
						switch (method.getName()) {
						case "open":
							fail(1);
							return null;
						case "close":
							synthClosed.incrementAndGet();
							return null;
						case "getReceiver":
							fail(2);
							receivers.incrementAndGet();
							return new Receiver() {
								@Override
								public void send(MidiMessage message, long timestamp) {
									output.send(message, timestamp);
								}

								@Override
								public void close() {
									receiverClosed.incrementAndGet();
								}
							};
						default:
							return defaultValue(method.getReturnType());
						}
					});
		}

		@Override
		Sequencer sequencer() {
			sequencers.incrementAndGet();
			return (Sequencer) Proxy.newProxyInstance(getClass().getClassLoader(),
					new Class<?>[] { Sequencer.class }, (proxy, method, args) -> {
						switch (method.getName()) {
						case "open":
							fail(3);
							return null;
						case "close":
							sequencerClosed.incrementAndGet();
							sequence = null;
							return null;
						case "setSequence":
							sequence = (Sequence) args[0];
							return null;
						case "setLoopCount":
							loopCount = (Integer) args[0];
							return null;
						case "start":
							running = true;
							starts.incrementAndGet();
							return null;
						case "stop":
							running = false;
							return null;
						case "isRunning":
							return running;
						case "getTransmitter":
							transmitters.incrementAndGet();
							return new Transmitter() {
								private Receiver target;

								@Override
								public void setReceiver(Receiver receiver) {
									if (failAt == 4)
										throw new IllegalStateException("injected connection failure");
									target = receiver;
								}

								@Override
								public Receiver getReceiver() {
									return target;
								}

								@Override
								public void close() {
									transmitterClosed.incrementAndGet();
								}
							};
						default:
							return defaultValue(method.getReturnType());
						}
					});
		}

		private void fail(int phase) throws MidiUnavailableException {
			if (failAt == phase)
				throw new MidiUnavailableException("injected open failure " + phase);
		}

		boolean allClosed() {
			return synths.get() == synthClosed.get() && sequencers.get() == sequencerClosed.get()
					&& receivers.get() == receiverClosed.get() && transmitters.get() == transmitterClosed.get();
		}

		private Object defaultValue(Class<?> type) {
			if (type == boolean.class)
				return false;
			if (type == int.class)
				return 0;
			if (type == long.class)
				return 0L;
			if (type == float.class)
				return 0F;
			return null;
		}
	}

	/**
	 * Records channel controllers and instrument state forwarded to the synthesizer.
	 *
	 * @author lare96
	 */
	private static final class Output implements Receiver {
		final AtomicInteger closed = new AtomicInteger();
		final int[][] controls = new int[16][128];
		final int[] program = new int[16];
		final int[] pitch = new int[16];

		@Override
		public synchronized void send(MidiMessage message, long timestamp) {
			if (message instanceof ShortMessage) {
				ShortMessage value = (ShortMessage) message;
				int channel = value.getChannel();
				if (value.getCommand() == ShortMessage.CONTROL_CHANGE)
					controls[channel][value.getData1()] = value.getData2();
				else if (value.getCommand() == ShortMessage.PROGRAM_CHANGE)
					program[channel] = value.getData1();
				else if (value.getCommand() == ShortMessage.PITCH_BEND)
					pitch[channel] = value.getData1() | (value.getData2() << 7);
			}
		}

		synchronized int volume(int channel) {
			return (controls[channel][7] << 7) | controls[channel][39];
		}

		synchronized int control(int channel, int controller) {
			return controls[channel][controller];
		}

		@Override
		public void close() {
			closed.incrementAndGet();
		}
	}
}
