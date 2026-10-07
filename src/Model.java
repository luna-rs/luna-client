/**
 * Represents a 3D mesh in the software rendering engine.
 *
 * <p>The Model class is the core of the 3D pipeline, responsible for storing vertex and face data,
 * performing spatial transformations (rotation, scaling, translation), and calculating lighting.
 * It utilizes a software-based rasterization approach, employing a "Painter's Algorithm"
 * with Z-sorting bins and priority layers to handle depth without a hardware Z-buffer.</p>
 *
 * <p>To maximize performance on legacy hardware, this class uses several optimizations:
 * <ul>
 *   <li>Static buffer pooling for projection and clipping to minimize Garbage Collection overhead.</li>
 *   <li>16-bit fixed-point arithmetic for trigonometric calculations and 3D projection.</li>
 *   <li>Parallel arrays for vertex and face attributes to improve cache locality.</li>
 * </ul></p>
 */
public class Model extends Entity {

	/**
	 * Constructs a "blank" or placeholder Model instance with default state flags.
	 *
	 * <p>This constructor does not load geometry data from the cache. It is primarily
	 * used for internal engine markers or as a base for models that will have their
	 * data assigned manually (e.g., procedurally generated meshes or scratchpad models).</p>
	 *
	 * <p>If the provided ID is invalid (<= 0), the {@code dummyMagicNumber} is shifted
	 * to a specific sentinel value (-110) to identify this as a system-level or
	 * null-model object.</p>
	 *
	 * @param id The unique identifier for this model instance.
	 */
	public Model(int id) {
		dummyVar = 932;
		dummVar2 = 426;
		isClickable = false;
		shadingEnabled = true;
		dummyMagicNumber = -252;
		isModified = false;
		isPriorityPicking = false;
		if (id <= 0)
			dummyMagicNumber = -110;
	}

	/**
	 * Constructs a new Model by deserializing raw data from the cache based on a header.
	 *
	 * <p>This constructor acts as the primary loader for model assets. it performs several
	 * complex decompression tasks to reconstruct the mesh:
	 * <ul>
	 *   <li><b>Delta Decoding:</b> Vertex coordinates are stored as relative offsets from
	 *       the previous vertex to save space. This constructor accumulates those offsets
	 *       into absolute 3D coordinates.</li>
	 *   <li><b>Topology Reconstruction:</b> Face indices are parsed using specific opcodes
	 *       (1-4) to determine if a face is an independent triangle, a triangle strip,
	 *        or a triangle fan.</li>
	 *   <li><b>Buffer Recycling:</b> To minimize memory overhead, it repositions existing
	 *       {@link JagBuffer} instances to point at different data segments (colors,
	 *       render types, priorities) within the same raw byte array.</li>
	 * </ul></p>
	 *
	 * @param modelId    The index of the model to load from the global {@link #modelHeaders} cache.
	 * @param dummyInt   A validation or toggle integer. If this value is greater than or
	 *                   equal to 0, the {@link #isModified} flag is toggled, signaling
	 *                   the engine that this model's geometry differs from the base cache version.
	 */
	public Model(int modelId, int dummyInt) {
		dummyVar = 932;
		dummVar2 = 426;

		isClickable = false;
		shadingEnabled = true;
		dummyMagicNumber = -252;
		isModified = false;
		isPriorityPicking = false;
		instanceCount++;

		ModelHeader modelHeader = Model.modelHeaders[modelId];
		verticesCount = modelHeader.vertexCount;
		faceCount = modelHeader.faceCount;
		textureVertexCount = modelHeader.textureVertexCount;

		// Allocate coordinate arrays
		verticesX = new int[verticesCount];
		verticesY = new int[verticesCount];
		verticesZ = new int[verticesCount];

		// Allocate face indexing arrays
		faceIndicesX = new int[faceCount];
		faceIndicesY = new int[faceCount];
		faceIndicesZ = new int[faceCount];

		// Allocate texture mapping arrays
		textureVertexIndicesA = new int[textureVertexCount];
		textureVertexIndicesB = new int[textureVertexCount];
		textureVertexIndicesC = new int[textureVertexCount];

		// Optional attribute allocation based on header offsets
		if (modelHeader.vertexBoneOffset >= 0) vertexBoneIds = new int[verticesCount];
		if (modelHeader.faceRenderTypeOffset >= 0) faceRenderTypes = new int[faceCount];
		if (modelHeader.facePriorityOffset >= 0) facePriorities = new int[faceCount];
		else {
			defaultPriority = -modelHeader.facePriorityOffset - 1;
		}

		if (modelHeader.faceTransparencyOffset >= 0) faceTransparency = new int[faceCount];
		if (modelHeader.faceBoneOffset >= 0) faceBoneIds = new int[faceCount];

		colors = new int[faceCount];

		// Initialize buffers for vertex data
		JagBuffer vertexFlagsBuffer = new JagBuffer(modelHeader.rawModelData);
		vertexFlagsBuffer.position = modelHeader.vertexFlagsOffset;

		JagBuffer vertexXBuffer = new JagBuffer(modelHeader.rawModelData);
		vertexXBuffer.position = modelHeader.vertexXOffset;

		JagBuffer vertexYBuffer = new JagBuffer(modelHeader.rawModelData);
		vertexYBuffer.position = modelHeader.vertexYOffset;


		if (dummyInt >= 0) //TODO REMOVE DUMMY, ITS USELESS
			isModified = !isModified;

		JagBuffer vertexZBuffer = new JagBuffer(modelHeader.rawModelData);
		vertexZBuffer.position = modelHeader.vertexZOffset;

		JagBuffer vertexBoneBuffer = new JagBuffer(modelHeader.rawModelData);
		vertexBoneBuffer.position = modelHeader.vertexBoneOffset;

		// --- Vertex Decoding (Delta Encoding) ---
		int lastX = 0;
		int lastY = 0;
		int lastZ = 0;

		for (int V = 0; V < verticesCount; V++) {

			int flag = vertexFlagsBuffer.getByte();

			int deltaX = 0;
			if ((flag & 1) != 0) {
				deltaX = vertexXBuffer.getSignedSmart();
			}

			int deltaY = 0;
			if ((flag & 2) != 0) {
				deltaY = vertexYBuffer.getSignedSmart();
			}

			int deltaZ = 0;
			if ((flag & 4) != 0) {
				deltaZ = vertexZBuffer.getSignedSmart();
			}

			verticesX[V] = lastX + deltaX;
			verticesY[V] = lastY + deltaY;
			verticesZ[V] = lastZ + deltaZ;

			lastX = verticesX[V];
			lastY = verticesY[V];
			lastZ = verticesZ[V];

			if (vertexBoneIds != null) {
				vertexBoneIds[V] = vertexBoneBuffer.getByte();
				}
		}

		// These buffers were used for vertices, now they are being pointed to face data blocks
		vertexFlagsBuffer.position = modelHeader.faceColorOffset;
		vertexXBuffer.position = modelHeader.faceRenderTypeOffset;
		vertexYBuffer.position = modelHeader.facePriorityOffset;
		vertexZBuffer.position = modelHeader.faceTransparencyOffset;
		vertexBoneBuffer.position = modelHeader.faceBoneOffset;

		for (int f = 0; f < faceCount; f++) {
			// vertexFlagsBuffer is now acting as the faceColorBuffer
			colors[f] = vertexFlagsBuffer.getShort();

			if (faceRenderTypes != null) faceRenderTypes[f] = vertexXBuffer.getByte();
			if (facePriorities != null) facePriorities[f] = vertexYBuffer.getByte();
			if (faceTransparency != null) faceTransparency[f] = vertexZBuffer.getByte();
			if (faceBoneIds != null) faceBoneIds[f] = vertexBoneBuffer.getByte();

		}

		vertexFlagsBuffer.position = modelHeader.faceIndicesOffset;
		vertexXBuffer.position = modelHeader.faceTypeOffset;

		int indexA = 0;
		int indexB = 0;
		int indexC = 0;
		int lastIndex = 0;

		for (int f = 0; f < faceCount; f++) {
			int topologyType = vertexXBuffer.getByte();
			if (topologyType == 1) { // New triangle
				indexA = vertexFlagsBuffer.getSignedSmart() + lastIndex;
				lastIndex = indexA;
				indexB = vertexFlagsBuffer.getSignedSmart() + lastIndex;
				lastIndex = indexB;
				indexC = vertexFlagsBuffer.getSignedSmart() + lastIndex;
				lastIndex = indexC;

				faceIndicesX[f] = indexA;
				faceIndicesY[f] = indexB;
				faceIndicesZ[f] = indexC;
			}
			if (topologyType == 2) { // Triangle Strip
				indexB = indexC;
				indexC = vertexFlagsBuffer.getSignedSmart() + lastIndex;
				lastIndex = indexC;
				faceIndicesX[f] = indexA;
				faceIndicesY[f] = indexB;
				faceIndicesZ[f] = indexC;
			}
			if (topologyType == 3) { // Triangle Fan
				indexA = indexC;
				indexC = vertexFlagsBuffer.getSignedSmart() + lastIndex;
				lastIndex = indexC;
				faceIndicesX[f] = indexA;
				faceIndicesY[f] = indexB;
				faceIndicesZ[f] = indexC;
			}
			if (topologyType == 4) { // Swapped/Mirrored Triangle
				int k4 = indexA;
				indexA = indexB;
				indexB = k4;
				indexC = vertexFlagsBuffer.getSignedSmart() + lastIndex;
				lastIndex = indexC;
				faceIndicesX[f] = indexA;
				faceIndicesY[f] = indexB;
				faceIndicesZ[f] = indexC;
			}
		}

		// --- Texture Mapping Decoding ---
		vertexFlagsBuffer.position = modelHeader.textureMappingOffset;
		for (int t = 0; t < textureVertexCount; t++) {
			textureVertexIndicesA[t] = vertexFlagsBuffer.getShort();
			textureVertexIndicesB[t] = vertexFlagsBuffer.getShort();
			textureVertexIndicesC[t] = vertexFlagsBuffer.getShort();
		}

	}

	/**
	 * Assembles multiple models into one, merging identical vertices to optimize the mesh.
	 *
	 * @param modelCount   The number of models to process from the table.
	 * @param sourceModels The array of models to be merged.
	 */
	public Model(int modelCount, Model[] sourceModels) {
		dummyVar = 932;
		dummVar2 = 426;
		isClickable = false;
		shadingEnabled = true;
		dummyMagicNumber = -252;
		isModified = false;
		isPriorityPicking = false;
		instanceCount++;

		boolean hasRenderTypes = false;
		boolean hasFacePriorities = false;
		boolean hasTransparency = false;
		boolean hasBoneIds = false;

		verticesCount = 0;
		faceCount = 0;
		textureVertexCount = 0;
		defaultPriority = -1;

		// --- Pass 1: Analysis ---
		// Determine the required capacity and which attribute arrays need to be allocated.
		for (int m = 0; m < modelCount; m++) {
			Model source = sourceModels[m];
			if (source != null) {
				verticesCount += source.verticesCount;
				faceCount += source.faceCount;
				textureVertexCount += source.textureVertexCount;

				hasRenderTypes |= source.faceRenderTypes != null;

				if (source.facePriorities != null) {
					hasFacePriorities = true;
				} else {
					// If sub-models have different default priorities,
					// we must use a full facePriorities array.
					if (defaultPriority == -1) {
						defaultPriority = source.defaultPriority;
					}
					if (defaultPriority != source.defaultPriority) {
						hasFacePriorities = true;
					}
				}
				hasTransparency |= source.faceTransparency != null;
				hasBoneIds |= source.faceBoneIds != null;
			}
		}

		// Allocate the combined arrays
		verticesX = new int[verticesCount];
		verticesY = new int[verticesCount];
		verticesZ = new int[verticesCount];
		vertexBoneIds = new int[verticesCount];
		faceIndicesX = new int[faceCount];
		faceIndicesY = new int[faceCount];
		faceIndicesZ = new int[faceCount];
		textureVertexIndicesA = new int[textureVertexCount];
		textureVertexIndicesB = new int[textureVertexCount];
		textureVertexIndicesC = new int[textureVertexCount];

		if (hasRenderTypes) faceRenderTypes = new int[faceCount];
		if (hasFacePriorities) facePriorities = new int[faceCount];
		if (hasTransparency) faceTransparency = new int[faceCount];
		if (hasBoneIds) faceBoneIds = new int[faceCount];

		colors = new int[faceCount];

		// Reset counters for the copy/merge pass
		verticesCount = 0;
		faceCount = 0;
		textureVertexCount = 0;

		int textureIndexOffset = 0;

		for (int m = 0; m < modelCount; m++) {
			Model source = sourceModels[m];

			if (source != null) {

				// Copy Faces and map their vertices
				for (int f = 0; f < source.faceCount; f++) {
					if (hasRenderTypes)
						if (source.faceRenderTypes == null) {
							faceRenderTypes[faceCount] = 0;
						} else {
							int renderType = source.faceRenderTypes[f];
							// Shift texture indices stored in the renderType bits
							if ((renderType & 2) == 2) {
								renderType += textureIndexOffset << 2;
							}
							faceRenderTypes[faceCount] = renderType;
						}

					if (hasFacePriorities) {
						if (source.facePriorities == null) {
							facePriorities[faceCount] = source.defaultPriority;
						} else {
							facePriorities[faceCount] = source.facePriorities[f];
						}
					}

					if (hasTransparency) {
						if (source.faceTransparency == null) {
							faceTransparency[faceCount] = 0;
						}
						else {
							faceTransparency[faceCount] = source.faceTransparency[f];
						}
					}

					if (hasBoneIds && source.faceBoneIds != null) {
						faceBoneIds[faceCount] = source.faceBoneIds[f];
					}

					colors[faceCount] = source.colors[f];

					// getOrMergeVertex handles the actual vertex data copying and deduplication
					faceIndicesX[faceCount] = getOrMergeVertex(source, source.faceIndicesX[f]);
					faceIndicesY[faceCount] = getOrMergeVertex(source, source.faceIndicesY[f]);
					faceIndicesZ[faceCount] = getOrMergeVertex(source, source.faceIndicesZ[f]);
					faceCount++;
				}

				// Copy Texture Indices and map their vertices
				for (int t = 0; t < source.textureVertexCount; t++) {
					textureVertexIndicesA[textureVertexCount] = getOrMergeVertex(source, source.textureVertexIndicesA[t]);
					textureVertexIndicesB[textureVertexCount] = getOrMergeVertex(source, source.textureVertexIndicesB[t]);
					textureVertexIndicesC[textureVertexCount] = getOrMergeVertex(source, source.textureVertexIndicesC[t]);
					textureVertexCount++;
				}

				textureIndexOffset += source.textureVertexCount;
			}
		}

	}

	/**
	 * Assembles multiple models into a single combined model.
	 *
	 * @param modelCount    The number of models from the table to merge.
	 * @param dummyFlag     A dummy boolean (unused in this implementation). //TODO REMOVE DUMMY
	 * @param dummyInt A validation integer (expected to be 0). //TODO REMOVE DUMMY
	 * @param sourceModels  An array of models to be merged into this instance.
	 */
	public Model(int modelCount, boolean dummyFlag, int dummyInt, Model[] sourceModels) {
		dummyVar = 932;
		dummVar2 = 426;
		isClickable = false;
		shadingEnabled = true;
		dummyMagicNumber = -252;
		isModified = false;
		isPriorityPicking = false;
		instanceCount++;

		boolean anyHasRenderTypes = false;
		boolean anyHasFacePriorities = false;
		boolean anyHasTransparency = false;
		boolean anyHasColors = false;

		verticesCount = 0;
		faceCount = 0;
		textureVertexCount = 0;
		defaultPriority = -1;

		// --- Pass 1: Analysis ---
		// Calculate total size and detect which attribute arrays need allocation.
		for (int m = 0; m < modelCount; m++) {
			Model model = sourceModels[m];
			if (model != null) {
				verticesCount += model.verticesCount;
				faceCount += model.faceCount;
				textureVertexCount += model.textureVertexCount;

				anyHasRenderTypes |= model.faceRenderTypes != null;

				if (model.facePriorities != null) {
					anyHasFacePriorities = true;
				} else {
					if (defaultPriority == -1) {
						defaultPriority = model.defaultPriority;
					}
					if (defaultPriority != model.defaultPriority) {
						anyHasFacePriorities = true;
					}
				}

				anyHasTransparency |= model.faceTransparency != null;
				anyHasColors |= model.colors != null;
			}
		}

		// Allocate merged arrays
		verticesX = new int[verticesCount];
		verticesY = new int[verticesCount];
		verticesZ = new int[verticesCount];
		faceIndicesX = new int[faceCount];
		faceIndicesY = new int[faceCount];
		faceIndicesZ = new int[faceCount];
		faceColorsA = new int[faceCount];
		faceColorsB = new int[faceCount];
		faceColorsC = new int[faceCount];
		textureVertexIndicesA = new int[textureVertexCount];
		textureVertexIndicesB = new int[textureVertexCount];
		textureVertexIndicesC = new int[textureVertexCount];

		if (anyHasRenderTypes) faceRenderTypes = new int[faceCount];
		if (anyHasFacePriorities) facePriorities = new int[faceCount];
		if (anyHasTransparency) faceTransparency = new int[faceCount];
		if (anyHasColors) colors = new int[faceCount];

		// Reset counters for the copy pass
		verticesCount = 0;
		faceCount = 0;
		textureVertexCount = 0;

		//TODO REMOVE
		if (dummyInt != 0)
			throw new NullPointerException();

		int textureOffset = 0;

		// --- Pass 2: Merging ---
		for (int m = 0; m < modelCount; m++) {
			Model source = sourceModels[m];
			if (source != null) {
				int baseVertexIndex = verticesCount;
				for (int v = 0; v < source.verticesCount; v++) {
					verticesX[verticesCount] = source.verticesX[v];
					verticesY[verticesCount] = source.verticesY[v];
					verticesZ[verticesCount] = source.verticesZ[v];
					verticesCount++;
				}

				// Copy Faces
				for (int f = 0; f < source.faceCount; f++) {
					// Offset the face indices so they point to the new vertex positions
					faceIndicesX[faceCount] = source.faceIndicesX[f] + baseVertexIndex;
					faceIndicesY[faceCount] = source.faceIndicesY[f] + baseVertexIndex;
					faceIndicesZ[faceCount] = source.faceIndicesZ[f] + baseVertexIndex;

					faceColorsA[faceCount] = source.faceColorsA[f];
					faceColorsB[faceCount] = source.faceColorsB[f];
					faceColorsC[faceCount] = source.faceColorsC[f];

					if (anyHasRenderTypes) {
						if (source.faceRenderTypes == null) {
							faceRenderTypes[faceCount] = 0;
						} else {
							int renderType = source.faceRenderTypes[f];
							// Shift texture indices stored in the renderType bits
							if ((renderType & 2) == 2) {
								renderType += textureOffset << 2;
							}
							faceRenderTypes[faceCount] = renderType;
						}
					}

					if (anyHasFacePriorities) {
						if (source.facePriorities == null) {
							facePriorities[faceCount] = source.defaultPriority;
						}
						else {
							facePriorities[faceCount] = source.facePriorities[f];
						}
					}

					if (anyHasTransparency) {
						if (source.faceTransparency == null) {
							faceTransparency[faceCount] = 0;
						}
						else {
							faceTransparency[faceCount] = source.faceTransparency[f];
							}
					}

					if (anyHasColors && source.colors != null) {
						colors[faceCount] = source.colors[f];
					}

					faceCount++;
				}

				// Copy Textures
				for (int t = 0; t < source.textureVertexCount; t++) {
					textureVertexIndicesA[textureVertexCount] = source.textureVertexIndicesA[t] + baseVertexIndex;
					textureVertexIndicesB[textureVertexCount] = source.textureVertexIndicesB[t] + baseVertexIndex;
					textureVertexIndicesC[textureVertexCount] = source.textureVertexIndicesC[t] + baseVertexIndex;
					textureVertexCount++;
				}

				textureOffset += source.textureVertexCount;
			}
		}

		calculateRadius();
	}

	/**
	 * Creates a new model derived from a source model, allowing for specific
	 * data arrays to be either shared by reference or deep-copied.
	 *
	 * @param shareVertices     If true, vertex coordinates are shared; if false, they are deep-copied.
	 * @param isStatic          If true, sets a specific sentinel value (498) often used for non-animated objects. MIGHT BE A DUMMY
	 * @param shareColors       If true, face colors are shared; if false, they are deep-copied.
	 * @param source            The template model to derive data from.
	 * @param shareTransparency If true, transparency values are shared; if false, they are deep-copied.
	 */
	public Model(boolean shareVertices, boolean isStatic, boolean shareColors,
				 Model source, boolean shareTransparency) {
		dummyVar = 932; //TODO REMOVE DUMMY
		dummVar2 = 426; //TODO REMOVE DUMMY
		isClickable = false;
		shadingEnabled = true;
		dummyMagicNumber = -252; //TODO REMOVE DUMMY
		isModified = false;
		isPriorityPicking = false;
		instanceCount++;

		verticesCount = source.verticesCount;
		faceCount = source.faceCount;
		textureVertexCount = source.textureVertexCount;


		if (isStatic) {
			dummyMagicNumber = 498;
		}

		// --- Vertex Handling ---
		if (shareVertices) {
			verticesX = source.verticesX;
			verticesY = source.verticesY;
			verticesZ = source.verticesZ;
		} else {
			verticesX = new int[verticesCount];
			verticesY = new int[verticesCount];
			verticesZ = new int[verticesCount];
			for (int v = 0; v < verticesCount; v++) {
				verticesX[v] = source.verticesX[v];
				verticesY[v] = source.verticesY[v];
				verticesZ[v] = source.verticesZ[v];
			}
		}

		// --- Color Handling ---
		if (shareColors) {
			colors = source.colors;
		} else {
			colors = new int[faceCount];
			for (int f = 0; f < faceCount; f++) {
				colors[f] = source.colors[f];
			}
		}

		// --- Transparency Handling ---
		if (shareTransparency) {
			faceTransparency = source.faceTransparency;
		} else {
			faceTransparency = new int[faceCount];
			if (source.faceTransparency == null) {
				for (int f = 0; f < faceCount; f++) {
					faceTransparency[f] = 0;
					}

			} else {
				for (int f = 0; f < faceCount; f++) {
					faceTransparency[f] = source.faceTransparency[f];
				}
			}
		}

		// --- Constant/Shared Attributes ---
		// These are typically indices or bone definitions that aren't
		// modified at runtime, so they are safe to share by reference.
		vertexBoneIds = source.vertexBoneIds;
		faceBoneIds = source.faceBoneIds;
		faceRenderTypes = source.faceRenderTypes;
		faceIndicesX = source.faceIndicesX;
		faceIndicesY = source.faceIndicesY;
		faceIndicesZ = source.faceIndicesZ;
		facePriorities = source.facePriorities;
		defaultPriority = source.defaultPriority;
		textureVertexIndicesA = source.textureVertexIndicesA;
		textureVertexIndicesB = source.textureVertexIndicesB;
		textureVertexIndicesC = source.textureVertexIndicesC;
	}

	public Model(boolean flag, boolean flag1, int i, Model model) {
		dummyVar = 932;
		dummVar2 = 426;
		isClickable = false;
		shadingEnabled = true;
		dummyMagicNumber = -252;
		isModified = false;
		isPriorityPicking = false;
		instanceCount++;
		verticesCount = model.verticesCount;
		faceCount = model.faceCount;
		textureVertexCount = model.textureVertexCount;
		if (flag) {
			verticesY = new int[verticesCount];
			for (int j = 0; j < verticesCount; j++)
				verticesY[j] = model.verticesY[j];

		} else {
			verticesY = model.verticesY;
		}
		if (flag1) {
			faceColorsA = new int[faceCount];
			faceColorsB = new int[faceCount];
			faceColorsC = new int[faceCount];
			for (int k = 0; k < faceCount; k++) {
				faceColorsA[k] = model.faceColorsA[k];
				faceColorsB[k] = model.faceColorsB[k];
				faceColorsC[k] = model.faceColorsC[k];
			}

			faceRenderTypes = new int[faceCount];
			if (model.faceRenderTypes == null) {
				for (int l = 0; l < faceCount; l++)
					faceRenderTypes[l] = 0;

			} else {
				for (int i1 = 0; i1 < faceCount; i1++)
					faceRenderTypes[i1] = model.faceRenderTypes[i1];

			}
			super.normals = new VertexNormal[verticesCount];
			for (int j1 = 0; j1 < verticesCount; j1++) {
				VertexNormal class40 = super.normals[j1] = new VertexNormal();
				VertexNormal class40_1 = ((Entity) (model)).normals[j1];
				class40.x = class40_1.x;
				class40.y = class40_1.y;
				class40.z = class40_1.z;
				class40.magnitude = class40_1.magnitude;
			}

			vertexNormalsTable = model.vertexNormalsTable;
		} else {
			faceColorsA = model.faceColorsA;
			faceColorsB = model.faceColorsB;
			faceColorsC = model.faceColorsC;
			faceRenderTypes = model.faceRenderTypes;
		}
		verticesX = model.verticesX;
		verticesZ = model.verticesZ;
		if (i != 0)
			isClickable = !isClickable;
		colors = model.colors;
		faceTransparency = model.faceTransparency;
		facePriorities = model.facePriorities;
		defaultPriority = model.defaultPriority;
		faceIndicesX = model.faceIndicesX;
		faceIndicesY = model.faceIndicesY;
		faceIndicesZ = model.faceIndicesZ;
		textureVertexIndicesA = model.textureVertexIndicesA;
		textureVertexIndicesB = model.textureVertexIndicesB;
		textureVertexIndicesC = model.textureVertexIndicesC;
		super.height = ((Entity) (model)).height;
		maxBottomExtent = model.maxBottomExtent;
		modelRadius = model.modelRadius;
		modelBoundingSphere = model.modelBoundingSphere;
		totalDepthSortingRange = model.totalDepthSortingRange;
		packedXBounds = model.packedXBounds;
		packedZBounds = model.packedZBounds;
		lightingParameters = model.lightingParameters;
	}

	/**
	 * The maximum value a vector component can have before its square
	 * risks overflowing a 32-bit integer during normalization.
	 * 8192^2 * 3 is approx 201M, well under the 2.1B integer limit.
	 */
	private static final int MAX_NORMAL_COMPONENT = 8192;
	/**
	 * Maximum number of transformation/bone groups that can be defined in a model.
	 */
	private static final int MAX_TRANSFORMATION_GROUPS = 256;

	/**
	 * The standard unit length for normalized vectors in this engine.
	 */
	private static final int NORMAL_SCALING_FACTOR = 256;
	/**
	 * Render type flag indicating flat shading (Bit 0 check).
	 */
	private static final int RENDER_TYPE_FLAT_SHADING = 0x1;

	/**
	 * Placeholder/dummy integer variable. Used in model initialization.
	 */
	public int dummyVar;
	/**
	 * Placeholder/dummy integer variable. Used in model initialization.
	 */
	public int dummVar2;
	/**
	 * Indicates whether this model is clickable for mouse interaction.
	 */
	public boolean isClickable;
	/**
	 * Flag to enable or disable shading calculations for this model.
	 */
	public boolean shadingEnabled;
	/**
	 * Placeholder/dummy magic number used for internal model state management.
	 */
	public int dummyMagicNumber;
	/**
	 * Flag indicating whether the model geometry has been modified since loading from cache.
	 */
	public boolean isModified;
	/**
	 * Global counter tracking the total number of Model instances created.
	 */
	public static int instanceCount;
	/**
	 * A reusable/scratch model instance for temporary operations to reduce memory allocation.
	 */
	public static Model SCRATCH_MODEL = new Model(852);
	/**
	 * Static vertex X-coordinate buffer for pooled model transformation.
	 * Reused across multiple models to minimize garbage collection.
	 */
	public static int[] staticVertexX = new int[2000];
	/**
	 * Static vertex Z-coordinate buffer for pooled model transformation.
	 * Reused across multiple models to minimize garbage collection.
	 */
	public static int[] staticVertexZ = new int[2000];
	/**
	 * Static vertex Y-coordinate buffer for pooled model transformation.
	 * Reused across multiple models to minimize garbage collection.
	 */
	public static int[] staticVertexY = new int[2000];
	/**
	 * Static face transparency buffer for pooled model transformation.
	 * Reused across multiple models to minimize garbage collection.
	 */
	public static int[] staticTransparency = new int[2000];
	/**
	 * The total number of vertices in this model.
	 */
	public int verticesCount;

	/**
	 * Vertex X-coordinates in local model space.
	 */
	public int[] verticesX;
	/**
	 * Vertex Y-coordinates in local model space.
	 */
	public int[] verticesY;
	/**
	 * Vertex Z-coordinates in local model space.
	 */
	public int[] verticesZ;

	/**
	 * Face vertex index A (first vertex of the triangle).
	 */
	public int[] faceIndicesX;
	/**
	 * Face vertex index B (second vertex of the triangle).
	 */
	public int[] faceIndicesY;
	/**
	 * Face vertex index C (third vertex of the triangle).
	 */
	public int[] faceIndicesZ;

	/**
	 * The total number of faces (triangles) in this model.
	 */
	public int faceCount;

	/**
	 * Face color for vertex A (first vertex). Used for Gouraud shading.
	 */
	public int[] faceColorsA;
	/**
	 * Face color for vertex B (second vertex). Used for Gouraud shading.
	 */
	public int[] faceColorsB;
	/**
	 * Face color for vertex C (third vertex). Used for Gouraud shading.
	 */
	public int[] faceColorsC;
	/**
	 * Face render types controlling shading and material properties.
	 * Bit 0 & 1 (& 3): Determines the Shading Type.
	 * 0: Gouraud (Smooth) Shading.
	 * 1: Flat Shading.
	 * 2 or 3: Textured/Mapping mode.
	 * Bit 2 (& 4): Often determines Color Behavior.
	 */
	public int[] faceRenderTypes;
	/**
	 * Priority level for each face, controlling z-ordering in the Painter's Algorithm.
	 * Valid range is typically 0-11 for multi-layer rendering.
	 */
	public int[] facePriorities;
	/**
	 * Transparency/alpha value for each face (0 = opaque, 255 = fully transparent).
	 */
	public int[] faceTransparency;
	/**
	 * Packed HSL color for each face. Used as fallback when per-vertex colors are unavailable.
	 */
	public int[] colors;
	/**
	 * Default priority level for all faces when facePriorities array is null.
	 */
	public int defaultPriority;
	/**
	 * The total number of texture vertices in this model.
	 */
	public int textureVertexCount;
	/**
	 * Texture vertex index A (first texture coordinate vertex).
	 */
	public int[] textureVertexIndicesA;
	/**
	 * Texture vertex index B (second texture coordinate vertex).
	 */
	public int[] textureVertexIndicesB;
	/**
	 * Texture vertex index C (third texture coordinate vertex).
	 */
	public int[] textureVertexIndicesC;
	/**
	 * Packed lighting parameters: High 16 bits are ambient, low 16 bits are light magnitude.
	 */
	public int lightingParameters;
	/**
	 * Packed X-axis bounds for AABB culling. High 16 bits = minX, low 16 bits = maxX.
	 */
	public int packedXBounds;
	/**
	 * Packed Z-axis bounds for AABB culling. High 16 bits = maxZ, low 16 bits = minZ.
	 */
	public int packedZBounds;
	/**
	 * Squared horizontal radius from origin (X^2 + Z^2) used for culling.
	 */
	public int modelRadius;
	/**
	 * Maximum extent below the model origin (positive Y direction).
	 */
	public int maxBottomExtent;
	/**
	 * Total depth range for z-sorting bin allocation in the Painter's Algorithm.
	 */
	public int totalDepthSortingRange;
	/**
	 * Radius of the bounding sphere encompassing the entire model.
	 */
	public int modelBoundingSphere;
	/**
	 * Height of the model (maximum extent in negative Y direction).
	 */
	public int modelHeight;
	/**
	 * Bone/transformation group IDs for each vertex. Used for skeletal animation.
	 */
	public int[] vertexBoneIds;
	/**
	 * Bone/transformation group IDs for each face. Used for skeletal animation.
	 */
	public int[] faceBoneIds;
	/**
	 * 2D array mapping bone/group IDs to arrays of vertex indices within that group.
	 * Allows efficient transformation of grouped vertices.
	 */
	public int[][] vertexIndicesByBone;
	/**
	 * 2D array mapping bone/group IDs to arrays of face indices within that group.
	 * Allows efficient transformation of grouped faces.
	 */
	public int[][] faceIndicesByBone;
	/**
	 * Flag indicating whether priority-based mouse picking is enabled for this model.
	 */
	public boolean isPriorityPicking;
	/**
	 * Array of vertex normals used for Gouraud/smooth shading calculations.
	 * Stored separately after lighting is baked for deferred shading.
	 */
	public VertexNormal[] vertexNormalsTable;
	/**
	 * Global array of model headers containing metadata about all loaded models.
	 */
	public static ModelHeader[] modelHeaders;
	/**
	 * Global provider instance responsible for asynchronously loading model data.
	 */
	public static ModelProvider modelProvider;
	/**
	 * Flags indicating which faces are completely off-screen (outside viewport).
	 */
	public static boolean[] faceIsOffScreen = new boolean[4096];
	/**
	 * Flags indicating which faces need near-plane clipping (cross the near-plane boundary).
	 */
	public static boolean[] faceNeedsClipping = new boolean[4096];
	/**
	 * Projected screen X-coordinates for all vertices after viewport transformation.
	 */
	public static int[] projectedX = new int[4096];
	/**
	 * Projected screen Y-coordinates for all vertices after viewport transformation.
	 */
	public static int[] projectedY = new int[4096];
	/**
	 * Projected screen Z-coordinates (depth) for all vertices after viewport transformation.
	 */
	public static int[] projectedZ = new int[4096];
	/**
	 * Camera-space X-coordinates for all vertices. Used for texture mapping and clipping.
	 */
	public static int[] cameraX = new int[4096];
	/**
	 * Camera-space Y-coordinates for all vertices. Used for texture mapping and clipping.
	 */
	public static int[] cameraY = new int[4096];
	/**
	 * Camera-space Z-coordinates (depth) for all vertices. Used for texture mapping and clipping.
	 */
	public static int[] cameraZ = new int[4096];
	/**
	 * Counter tracking the number of faces at each depth level in the Painter's Algorithm.
	 */
	public static int[] faceDepthCounts = new int[1500];
	/**
	 * 2D array storing face indices binned by their depth for the Painter's Algorithm.
	 * Each depth level can contain up to 512 faces.
	 */
	public static int[][] faceDepthBins = new int[1500][512];
	/**
	 * Counter tracking the number of faces at each priority level (0-11).
	 */
	public static int[] priorityCounts = new int[12];
	/**
	 * 2D array storing face indices binned by their priority level (0-11).
	 * Each priority level can contain up to 2000 faces.
	 */
	public static int[][] priorityBins = new int[12][2000];
	/**
	 * Depth values for high-priority faces (priority level 10) used in interleaved rendering.
	 */
	public static int[] priorityDepthX = new int[2000];
	/**
	 * Depth values for highest-priority faces (priority level 11) used in interleaved rendering.
	 */
	public static int[] priorityDepthY = new int[2000];
	/**
	 * Accumulated depth values for priority levels 0-9 to calculate average depth thresholds.
	 */
	public static int[] priorityAverages = new int[12];
	/**
	 * Clipped projected X-coordinates for near-plane clipped polygons (up to 4 vertices per face).
	 */
	public static int[] clippedProjectedX = new int[10];
	/**
	 * Clipped projected Y-coordinates for near-plane clipped polygons (up to 4 vertices per face).
	 */
	public static int[] clippedProjectedY = new int[10];
	/**
	 * Vertex colors for clipped polygon vertices after near-plane intersection calculations.
	 */
	public static int[] clippedVertexColors = new int[10];
	/**
	 * X-coordinate of the current transformation pivot point (used for rotation/scaling centers).
	 */
	public static int transformationPivotX;
	/**
	 * Y-coordinate of the current transformation pivot point (used for rotation/scaling centers).
	 */
	public static int transformationPivotY;
	/**
	 * Z-coordinate of the current transformation pivot point (used for rotation/scaling centers).
	 */
	public static int transformationPivotZ;
	/**
	 * Global flag enabling/disabling mouse-based model picking functionality.
	 */
	public static boolean isPickingEnabled;
	/**
	 * Current mouse X-coordinate on screen (used for picking detection).
	 */
	public static int mouseX;
	/**
	 * Current mouse Y-coordinate on screen (used for picking detection).
	 */
	public static int mouseY;
	/**
	 * Number of models currently under the mouse cursor (hovered count).
	 */
	public static int hoveredCount;
	/**
	 * Array storing the IDs of all models currently hovered by the mouse.
	 */
	public static int[] hoveredModels = new int[1000];
	/**
	 * Pre-calculated sine values table indexed by angle (0-2047).
	 * Shared across all engine components for performance.
	 */
	public static int[] sineTable;
	/**
	 * Pre-calculated cosine values table indexed by angle (0-2047).
	 * Shared across all engine components for performance.
	 */
	public static int[] cosineTable;
	/**
	 * Lookup table converting packed HSL colors to RGB values.
	 * Shared across all engine components for color conversion.
	 */
	public static int[] colorLookupTable;
	/**
	 * Pre-calculated reciprocal (1/x) values table for fast division in fixed-point arithmetic.
	 * Used in clipping calculations and perspective division.
	 */
	public static int[] reciprocalTable;

	static {
		sineTable = ThreeDimensionalCanvas.sineTable;
		cosineTable = ThreeDimensionalCanvas.cosineTable;
		colorLookupTable = ThreeDimensionalCanvas.anIntArray1548;
		reciprocalTable = ThreeDimensionalCanvas.divTable;
	}

	/**
	 * Nullifies all global static buffers to assist the Garbage Collector in reclaiming memory.
	 *
	 * @param preserveMathTables If true, shared mathematical tables (sine, cosine, reciprocal)
	 *                           remain in memory for other engine components to use.
	 */
	public static void dispose(boolean preserveMathTables) {
		// Only clear the shared reciprocal table if explicitly requested
		if (!preserveMathTables) {
			reciprocalTable = null;;
		}

		modelHeaders = null;
		faceIsOffScreen = null;
		faceNeedsClipping = null;
		projectedX = null;
		projectedY = null;
		projectedZ = null;
		cameraX = null;
		cameraY = null;
		cameraZ = null;
		//Clear depth sorting (Z-buffer) bins
		faceDepthCounts = null;
		faceDepthBins = null;
		//Clear priority-based rendering pools
		priorityCounts = null;
		priorityBins = null;
		priorityDepthX = null;
		priorityDepthY = null;
		priorityAverages = null;

		//Clear global lookup tables
		sineTable = null;
		cosineTable = null;
		colorLookupTable = null;
	}

	/**
	 * Initializes the global model system.
	 *
	 * @param modelCount The total number of models present in the cache.
	 * @param provider   The provider responsible for loading/requesting model data.
	 */
	public static void init(int modelCount, ModelProvider provider) {
		modelHeaders = new ModelHeader[modelCount];
		modelProvider = provider;
	}

	/**
	 * Unpacks raw byte data into a {@link ModelHeader} object.
	 *
	 * <p>This method reads metadata and data block offsets from the provided byte array
	 * and populates a {@code ModelHeader} instance, which then describes the structure
	 * of the model's raw data. This header is crucial for the {@link Model} constructor
	 * to correctly interpret and load the model's vertices, faces, and other attributes.</p>
	 *
	 * @param modelDataBytes The raw byte array containing the model's header and data.
	 * @param modelId        The unique identifier for the model being unpacked.
	 * @param dummyByte a dummy Byte (expected to be 7) to ensure proper method invocation.
	 */
	public static void unpackModelHeader(byte[] modelDataBytes, int modelId, byte dummyByte) {
		// Validate the invocation byte
		if (dummyByte != 7)
			return;

		// If no data is provided, create an empty model header
		if (modelDataBytes == null) {
			ModelHeader modelHeader = Model.modelHeaders[modelId] = new ModelHeader();
			modelHeader.vertexCount = 0;
			modelHeader.faceCount = 0;
			modelHeader.textureVertexCount = 0;
			return;
		}

		// Create a buffer to read from the model data bytes
		JagBuffer dataBuffer = new JagBuffer(modelDataBytes);
		// Position the buffer to read the header information, which is typically at the end
		dataBuffer.position = modelDataBytes.length - 18;

		// Create and store the new ModelHeader in the global cache
		ModelHeader modelHeader = modelHeaders[modelId] = new ModelHeader();
		modelHeader.rawModelData = modelDataBytes;

		// Read fundamental counts
		modelHeader.vertexCount = dataBuffer.getShort();
		modelHeader.faceCount = dataBuffer.getShort();
		modelHeader.textureVertexCount = dataBuffer.getByte();

		// Read flags indicating the presence of optional data blocks
		int hasFaceRenderTypesFlag = dataBuffer.getByte();
		int facePriorityFlag = dataBuffer.getByte();
		int hasFaceTransparencyFlag = dataBuffer.getByte();
		int hasFaceBoneIdsFlag = dataBuffer.getByte();
		int hasVertexBoneIdsFlag = dataBuffer.getByte();

		// Read lengths of various data blocks
		int vertexXDataLength = dataBuffer.getShort();
		int vertexYDataLength = dataBuffer.getShort();
		int vertexZDataLength = dataBuffer.getShort();
		int faceIndicesDataLength = dataBuffer.getShort();

		// Calculate and assign offsets for each data block
		int currentOffset = 0;
		modelHeader.vertexFlagsOffset = currentOffset;
		currentOffset += modelHeader.vertexCount;

		modelHeader.faceTypeOffset = currentOffset;
		currentOffset += modelHeader.faceCount;

		modelHeader.facePriorityOffset = currentOffset;
		// If 255, it means there's a dedicated priority block
		if (facePriorityFlag == 255) {
			currentOffset += modelHeader.faceCount;
		}
		// Otherwise, the flag itself indicates a default priority
		else {
			modelHeader.facePriorityOffset = -facePriorityFlag - 1;
		}

		modelHeader.faceBoneOffset = currentOffset;
		if (hasFaceBoneIdsFlag == 1) {
			currentOffset += modelHeader.faceCount;
		}
		else {
			modelHeader.faceBoneOffset = -1;
		}

		modelHeader.faceRenderTypeOffset = currentOffset;
		if (hasFaceRenderTypesFlag == 1) {
			currentOffset += modelHeader.faceCount;
		}
		else {
			modelHeader.faceRenderTypeOffset = -1;
		}

		modelHeader.vertexBoneOffset = currentOffset;
		if (hasVertexBoneIdsFlag == 1) {
			currentOffset += modelHeader.vertexCount;
		}
		else {
			modelHeader.vertexBoneOffset = -1;
		}

		modelHeader.faceTransparencyOffset = currentOffset;
		if (hasFaceTransparencyFlag == 1) {
			currentOffset += modelHeader.faceCount;
		}
		else {
			modelHeader.faceTransparencyOffset = -1;
		}

		modelHeader.faceIndicesOffset = currentOffset;
		currentOffset += faceIndicesDataLength;

		modelHeader.faceColorOffset = currentOffset;
		currentOffset += modelHeader.faceCount * 2; // 2 bytes per color (short)

		modelHeader.textureMappingOffset = currentOffset;
		currentOffset += modelHeader.textureVertexCount * 6; // 6 bytes per texture vertex (3 shorts)

		modelHeader.vertexXOffset = currentOffset;
		currentOffset += vertexXDataLength;

		modelHeader.vertexYOffset = currentOffset;
		currentOffset += vertexYDataLength;

		modelHeader.vertexZOffset = currentOffset;
		currentOffset += vertexZDataLength; // Final offset, no need to increment further
	}

	/**
	 * Unloads a model header from the global cache.
	 *
	 * <p>This removes the metadata associated with the model ID, allowing the
	 * memory to be reclaimed. If the model is needed again, it will be
	 * re-requested through the {@code ModelProvider}.</p>
	 *
	 * @param modelId The unique identifier of the model to unload.
	 */
	public static void unloadModelHeader(int modelId) {
			modelHeaders[modelId] = null;
	}

	/**
	 * Retrieves a model instance by its ID.
	 *
	 * <p>If the model's metadata is not yet loaded, this method will trigger
	 * an asynchronous request via the {@code modelProvider} and return null.</p>
	 *
	 * @param id The unique identifier of the model to load.
	 * @return A new {@code Model} instance if the header is available; otherwise null.
	 */
	public static Model forId(int id) {
		if (modelHeaders == null) {
			return null;
		}

		ModelHeader modelHeader = Model.modelHeaders[id];

		// Check if the model metadata (header) is present in the cache
		if (modelHeader == null) {
			modelProvider.requestModel(id);
			return null;
		}

		return new Model(id, -478);

	}

	public static boolean isDownloaded(int id) {
		if (modelHeaders == null)
			return false;
		ModelHeader modelHeader = Model.modelHeaders[id];
		if (modelHeader == null) {
			modelProvider.requestModel(id);
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Copies the geometry and metadata from a source model into this instance.
	 *
	 * <p>To optimize performance and minimize Garbage Collection, this method utilizes
	 * static buffer pooling for vertex coordinates and transparency. While vertex
	 * positions are deep-copied into these buffers, many other attributes (indices,
	 * colors, bones) are shallow-copied by reference.</p>
	 *
	 * @param shareTransparency If true, the transparency array is shared by reference;
	 *                          if false, it is copied into a static buffer.
	 * @param source            The source model to copy data from.
	 */
	public void copyFrom(boolean shareTransparency, Model source) {
		verticesCount = source.verticesCount;
		faceCount = source.faceCount;
		textureVertexCount = source.textureVertexCount;

		// --- Vertex Buffer Pooling ---
		// Ensure the static global buffers are large enough to hold the incoming data
		if (staticVertexX.length < verticesCount) {
			staticVertexX = new int[verticesCount + 100];
			staticVertexY = new int[verticesCount + 100];
			staticVertexZ = new int[verticesCount + 100];
		}

		// Assign this model's vertex pointers to the global static pool
		verticesX = staticVertexX;
		verticesY = staticVertexY;
		verticesZ = staticVertexZ;

		// Deep copy vertex coordinates so this model can be animated
		// independently of the source model.
		for (int j = 0; j < verticesCount; j++) {
			verticesX[j] = source.verticesX[j];
			verticesY[j] = source.verticesY[j];
			verticesZ[j] = source.verticesZ[j];
		}

		// --- Transparency Management ---
		if (shareTransparency) {
			faceTransparency = source.faceTransparency;
		} else {
			if (staticTransparency.length < faceCount)
				staticTransparency = new int[faceCount + 100];
			faceTransparency = staticTransparency;
			if (source.faceTransparency == null) {
				for (int f = 0; f < faceCount; f++)
					faceTransparency[f] = 0;

			} else {
				for(int f = 0; f < faceCount; f++)
					faceTransparency[f] = source.faceTransparency[f];

			}
		}

		// --- Shallow Copy (Shared References) ---
		// These values are generally not modified during animation,
		// so it is safe to share the reference.
		faceRenderTypes = source.faceRenderTypes;
		colors = source.colors;
		facePriorities = source.facePriorities;
		defaultPriority = source.defaultPriority;
		faceIndicesByBone = source.faceIndicesByBone;
		vertexIndicesByBone = source.vertexIndicesByBone;
		faceIndicesX = source.faceIndicesX;
		faceIndicesY = source.faceIndicesY;
		faceIndicesZ = source.faceIndicesZ;
		faceColorsA = source.faceColorsA;
		faceColorsB = source.faceColorsB;
		faceColorsC = source.faceColorsC;
		textureVertexIndicesA = source.textureVertexIndicesA;
		textureVertexIndicesB = source.textureVertexIndicesB;
		textureVertexIndicesC = source.textureVertexIndicesC;
	}

	/**
	 * Merges a vertex from a source model into the current model, preventing duplicates.
	 *
	 * <p>This method performs a linear search to see if a vertex with identical coordinates
	 * already exists in the current vertex pool. If found, it returns the existing index.
	 * Otherwise, it appends the vertex to the pool and returns the new index.</p>
	 *
	 * @param sourceModel       The model containing the vertex to be copied.
	 * @param sourceVertexIndex The index of the vertex in the source model.
	 * @return The index of the vertex within the current model.
	 */
	public int getOrMergeVertex(Model sourceModel, int sourceVertexIndex) {
		int existingIndex = -1;

		// Extract coordinates from the source model
		int sourceX = sourceModel.verticesX[sourceVertexIndex];
		int sourceY = sourceModel.verticesY[sourceVertexIndex];
		int sourceZ = sourceModel.verticesZ[sourceVertexIndex];

		// Search for an identical vertex in the current model
		for (int v = 0; v < verticesCount; v++) {
			if (sourceX != verticesX[v] || sourceY != verticesY[v] || sourceZ != verticesZ[v])
				continue;
			existingIndex = v;
			break;
		}

		// If the vertex was not found, append it to the current model's buffers
		if (existingIndex == -1) {
			verticesX[verticesCount] = sourceX;
			verticesY[verticesCount] = sourceY;
			verticesZ[verticesCount] = sourceZ;
			if (sourceModel.vertexBoneIds != null)
				vertexBoneIds[verticesCount] = sourceModel.vertexBoneIds[sourceVertexIndex];
			existingIndex = verticesCount++;
		}
		return existingIndex;
	}

	public void calculateRadius() {
		super.height = 0;
		modelRadius = 0;
		maxBottomExtent = 0;
		for (int j = 0; j < verticesCount; j++) {
			int k = verticesX[j];
			int l = verticesY[j];
			int i1 = verticesZ[j];
			if (-l > super.height)
				super.height = -l;
			if (l > maxBottomExtent)
				maxBottomExtent = l;
			int j1 = k * k + i1 * i1;
			if (j1 > modelRadius)
				modelRadius = j1;
		}

		modelRadius = (int) (Math.sqrt(modelRadius) + 0.98999999999999999D);
		modelBoundingSphere = (int) (Math.sqrt(modelRadius * modelRadius + super.height * super.height) + 0.98999999999999999D);
		totalDepthSortingRange = modelBoundingSphere + (int) (Math.sqrt(modelRadius * modelRadius + maxBottomExtent * maxBottomExtent) + 0.98999999999999999D);
	}

	public void updateVerticalBounds() {
		// Reset vertical extents
		super.height = 0; // Max distance above origin (negative Y)
		maxBottomExtent = 0; // Max distance below origin (positive Y)

		// Scan only the Y-axis
		for (int v = 0; v < verticesCount; v++) {
			int y = verticesY[v];
			if (-y > super.height)
				super.height = -y;
			if (y > maxBottomExtent)
				maxBottomExtent = y;
		}

		// Recalculate the 3D Bounding Sphere Radius
		// Using 0.99D before casting to (int) is a legacy "ceiling" trick to ensure the sphere
		// always fully encompasses the model vertices.
		modelBoundingSphere = (int) (Math.sqrt(modelRadius * modelRadius + super.height * super.height) + 0.99D);

		// Recalculate the total depth range for Z-sorting bins
		// This is (Distance to top-most point) + (Distance to bottom-most point)
		totalDepthSortingRange = modelBoundingSphere + (int) (Math.sqrt(modelRadius * modelRadius + maxBottomExtent * maxBottomExtent) + 0.99D);
	}

	/**
	 * Calculates the Axis-Aligned Bounding Box (AABB) and the bounding sphere radii for the model.
	 *
	 * <p>This method iterates through all vertices to find the minimum and maximum extents
	 * on all three axes. It also calculates the maximum horizontal distance from the origin
	 * to determine the model's circular and spherical bounds.</p>
	 */
	public void calculateAABB() {
		// Reset vertical extents
		super.height = 0; // Represents the maximum extent above the origin (negative Y)
		maxBottomExtent = 0; // Represents the maximum extent below the origin (positive Y)
		modelRadius = 0; // Square of the horizontal radius (X^2 + Z^2)


		int minX = Short.MAX_VALUE;
		int maxX = Short.MIN_VALUE;

		int minZ = Short.MAX_VALUE;
		int maxZ = Short.MIN_VALUE;

		for (int v = 0; v < verticesCount; v++) {
			int x = verticesX[v];
			int y = verticesY[v];
			int z = verticesZ[v];

			// Update Horizontal Bound Extremes
			if (x < minX) minX = x;
			if (x > maxX) maxX = x;
			if (z < minZ) minZ = z;
			if (z > maxZ) maxZ = z;

			// Update Vertical Extents
			// In this engine's coordinate system, negative Y is "up"
			if (-y > super.height) {
				super.height = -y;
			}
			if (y > maxBottomExtent) {
				maxBottomExtent = y;
			}

			// Track the squared horizontal distance from the origin
			int horizontalDistanceSq = x * x + z * z;
			if (horizontalDistanceSq > modelRadius) {
				modelRadius = horizontalDistanceSq;
			}
		}

		// Convert squared horizontal distance to actual radius
		modelRadius = (int) Math.sqrt(modelRadius);

		// anInt1674: The 3D radius from origin to the furthest top vertex
		modelBoundingSphere = (int) Math.sqrt(modelRadius * modelRadius + super.height * super.height);

		// The total depth range used for Z-buffer/Bin allocation
		// Calculated as the distance to the furthest top point + distance to furthest bottom point
		totalDepthSortingRange = modelBoundingSphere + (int) Math.sqrt(modelRadius * modelRadius + maxBottomExtent * maxBottomExtent);

		// Pack the AABB boundaries into 32-bit integers for high-performance culling checks
		// packedXBounds stores X-axis bounds (min in high bits, max in low bits)
		packedXBounds = (minX << 16) + (maxX & 0xffff);

		// packedZBounds stores Z-axis bounds (max in high bits, min in low bits)
		packedZBounds = (maxZ << 16) + (minZ & 0xffff);
	}

	/**
	 * Groups vertex and face indices by their respective transformation/bone IDs.
	 *
	 * <p>This method converts flat mapping arrays (vertexGroups and faceGroups) into
	 * structured 2D arrays (groupedVertexIndices and groupedFaceIndices). This
	 * allows the animation system to transform groups of geometry efficiently
	 * without scanning the entire model.</p>
	 */
	public void groupIndicesByTransform() {

		// Group Vertex Indices by Bone/Transformation ID
		if (vertexBoneIds != null) {
			int[] groupCounts = new int[MAX_TRANSFORMATION_GROUPS];
			int maxGroupId = 0;

			// Count occurrences of each group ID to determine sub-array sizes
			for (int v = 0; v < verticesCount; v++) {
				int groupId = vertexBoneIds[v];
				groupCounts[groupId]++;
				if (groupId > maxGroupId)
					maxGroupId = groupId;
			}

			// Allocate the outer 2D array based on the highest Group ID found
			vertexIndicesByBone = new int[maxGroupId + 1][];
			for (int g = 0; g <= maxGroupId; g++) {
				vertexIndicesByBone[g] = new int[groupCounts[g]];
				groupCounts[g] = 0;
			}

			for (int v = 0; v < verticesCount; v++) {
				int groupId = vertexBoneIds[v];
				vertexIndicesByBone[groupId][groupCounts[groupId]++] = v;
			}

			vertexBoneIds = null;
		}

		// Group Face Indices by Bone/Transformation ID
		if (faceBoneIds != null) {
			int[] groupCounts = new int[MAX_TRANSFORMATION_GROUPS];
			int maxGroupId = 0;

			// Count occurrences for faces
			for (int f = 0; f < faceCount; f++) {
				int groupId = faceBoneIds[f];
				groupCounts[groupId]++;
				if (groupId > maxGroupId)
					maxGroupId = groupId;
			}

			// Allocate grouped face index table
			faceIndicesByBone = new int[maxGroupId + 1][];
			for (int g = 0; g <= maxGroupId; g++) {
				faceIndicesByBone[g] = new int[groupCounts[g]];
				groupCounts[g] = 0;
			}

			// Populate face indices
			for (int f = 0; f < faceCount; f++) {
				int groupId = faceBoneIds[f];
				faceIndicesByBone[groupId][groupCounts[groupId]++] = f;
			}

			// Nullify the flat array to reclaim memory
			faceBoneIds = null;
		}
	}

	/**
	 * Applies a single animation frame to the model.
	 *
	 * <p>This method iterates through all transformation instructions stored in the
	 * specified frame and applies them to the corresponding vertex/bone groups.</p>
	 *
	 * @param frameId        The ID of the {@link AnimationFrame} to apply.
	 * @param dummyByte A dummy byte (expected to be 6) used to ensure
	 *                       internal calling consistency.
	 */
	public void applyAnimation(int frameId, byte dummyByte) {
		// Cannot animate if the model hasn't been grouped by bones
		if (vertexIndicesByBone == null) {
			return;
		}

		if (frameId == -1) {
			return;
		}

		AnimationFrame frame = AnimationFrame.forId(frameId);
		if (frame == null) {
			return;
		}

		Skeleton skeleton = frame.skeleton;

		// Standard engine-specific validation check
		if (dummyByte == 6) {
			dummyByte = 0;
		}
		else {
			return;
		}

		// Reset pivots before starting a new frame application
		transformationPivotX = 0;
		transformationPivotY = 0;
		transformationPivotZ = 0;

		for (int i = 0; i < frame.instructionCount; i++) {
			int instructionIdx = frame.instructionIndices[i];
			applyTransformation(skeleton.opcodes[instructionIdx], skeleton.boneGroups[instructionIdx], frame.transformationX[i],
					frame.transformationY[i], frame.transformationZ[i]);
		}

	}

	/**
	 * Applies a blend of two animation frames based on a mask.
	 *
	 * @param secondaryFrameId The ID of the frame to apply to masked bones.
	 * @param dummy            A dummy value used to toggle internal state.
	 * @param primaryFrameId   The ID of the frame to apply to unmasked bones.
	 * @param mask             An array of instruction indices that should use the secondary frame.
	 */
	public void applyBlendedAnimation(int secondaryFrameId, int dummy, int primaryFrameId, int[] mask) {
		if (primaryFrameId == -1)
			return;

		// If no mask is provided or no secondary frame exists, default to standard animation
		if (mask == null || secondaryFrameId == -1) {
			applyAnimation(primaryFrameId, (byte) 6);
			return;
		}

		AnimationFrame primaryFrame = AnimationFrame.forId(primaryFrameId);
		if (primaryFrame == null) {
			return;
		}

		AnimationFrame secondaryFrame = AnimationFrame.forId(secondaryFrameId);
		if (secondaryFrame == null) {
			applyAnimation(primaryFrameId, (byte) 6);
			return;
		}

		Skeleton skeleton = primaryFrame.skeleton;
		transformationPivotX = 0;

		if (dummy != 0)
			isModified = !isModified;

		transformationPivotY = 0;
		transformationPivotZ = 0;

		int maskPtr = 0;
		int maskValue = mask[maskPtr++];

		// --- Part 1: Apply Primary Frame to UNMASKED bones ---
		for (int i = 0; i < primaryFrame.instructionCount; i++) {
			int instructionIdx;
			// Advance the mask pointer until it reaches or exceeds the current instruction
			for (instructionIdx = primaryFrame.instructionIndices[i]; instructionIdx > maskValue; maskValue = mask[maskPtr++]);

			// If this instruction is NOT in the mask, or it is a pivot opcode (0), apply it
			if (instructionIdx != maskValue || skeleton.opcodes[instructionIdx] == 0) {
				applyTransformation(skeleton.opcodes[instructionIdx], skeleton.boneGroups[instructionIdx], primaryFrame.transformationX[i],
						primaryFrame.transformationY[i], primaryFrame.transformationZ[i]);
			}
		}

		// Reset pivot for the second pass
		transformationPivotX = 0;
		transformationPivotY = 0;
		transformationPivotZ = 0;
		maskPtr = 0;
		maskValue = mask[maskPtr++];

		// --- Part 2: Apply Secondary Frame to MASKED bones ---
		for (int i = 0; i < secondaryFrame.instructionCount; i++) {
			int instructionIdx;
			// Advance the mask pointer until it reaches or exceeds the current instruction
			for (instructionIdx = secondaryFrame.instructionIndices[i]; instructionIdx > maskValue; maskValue = mask[maskPtr++]);

			// If this instruction IS in the mask, or it is a pivot opcode (0), apply it
			if (instructionIdx == maskValue || skeleton.opcodes[instructionIdx] == 0)
				applyTransformation(skeleton.opcodes[instructionIdx], skeleton.boneGroups[instructionIdx], secondaryFrame.transformationX[i],
						secondaryFrame.transformationY[i], secondaryFrame.transformationZ[i]);
		}

	}



	/**
	 * Applies a specific transformation to grouped vertices or faces based on a provided opcode.
	 *
	 * <p>This method is the core of the skeletal animation system. It manipulates the model's
	 * geometry or attributes by targeting specific "bone groups" (sets of indices). It handles
	 * pivot calculation, translation, rotation, scaling, and transparency modifications using
	 * fixed-point arithmetic.</p>
	 *
	 * <p>The behavior depends on the {@code opcode}:</p>
	 * <ul>
	 *   <li><b>Opcode 0 (Pivot Calculation):</b> Calculates the centroid (average position) of
	 *       all vertices in the specified bone groups and stores it in the global
	 *       {@code transformationPivot} variables. This pivot is used for subsequent rotations and scales.</li>
	 *   <li><b>Opcode 1 (Translation):</b> Offsets the X, Y, and Z coordinates of the targeted vertices.</li>
	 *   <li><b>Opcode 2 (Rotation):</b> Rotates targeted vertices around the current
	 *       {@code transformationPivot}. Angles are provided as 8-bit values (0-255) and
	 *       internalized using fixed-point sine/cosine tables.</li>
	 *   <li><b>Opcode 3 (Scaling):</b> Resizes targeted vertices relative to the
	 *       {@code transformationPivot}. The transformation values represent a percentage
	 *       multiplier where 128 is 100% scale.</li>
	 *   <li><b>Opcode 5 (Transparency):</b> Modifies the alpha transparency of faces assigned
	 *       to the specified bone groups.</li>
	 * </ul>
	 *
	 * @param opcode         The transformation type to perform (0, 1, 2, 3, or 5).
	 * @param boneGroupIds   An array of bone/group identifiers defining which part of the
	 *                       mesh is affected.
	 * @param transformX     The X-axis transformation value (translation delta, rotation angle,
	 *                       scale factor, or transparency delta).
	 * @param transformY     The Y-axis transformation value.
	 * @param transformZ     The Z-axis transformation value.
	 */
	public void applyTransformation(int opcode, int[] boneGroupIds, int transformX, int transformY, int transformZ) {
		int groupCount = boneGroupIds.length;

		//TODO COULD BE A SWITCH OF OPCODES INSTEAD OF IF-ELSES;

		// OPCODE 0: Calculate Transformation Pivot (Centroid of specified bones)
		if (opcode == 0) {
			int totalVertices = 0;
			transformationPivotX = 0;
			transformationPivotY = 0;
			transformationPivotZ = 0;
			for (int i = 0; i < groupCount; i++) {
				int boneId = boneGroupIds[i];
				if (boneId < vertexIndicesByBone.length) {
					int[] vertexIndices = vertexIndicesByBone[boneId];
					for (int v = 0; v < vertexIndices.length; v++) {
						int vertexId = vertexIndices[v];
						transformationPivotX += verticesX[vertexId];
						transformationPivotY += verticesY[vertexId];
						transformationPivotZ += verticesZ[vertexId];
						totalVertices++;
					}
				}
			}

			if (totalVertices > 0) {
				transformationPivotX = transformationPivotX / totalVertices + transformX;
				transformationPivotY = transformationPivotY / totalVertices + transformY;
				transformationPivotZ = transformationPivotZ / totalVertices + transformZ;
				return;
			} else {
				transformationPivotX = transformX;
				transformationPivotY = transformY;
				transformationPivotZ = transformZ;
				return;
			}
		}

		// OPCODE 1: Translation (Movement)
		if (opcode == 1) {
			for (int i = 0; i < groupCount; i++) {
				int boneId = boneGroupIds[i];
				if (boneId < vertexIndicesByBone.length) {
					int[] vertexIndices = vertexIndicesByBone[boneId];
					for (int v = 0; v < vertexIndices.length; v++) {
						int vertexId = vertexIndices[v];
						verticesX[vertexId] += transformX;
						verticesY[vertexId] += transformY;
						verticesZ[vertexId] += transformZ;
					}
				}
			}
			return;
		}

		// OPCODE 2: Rotation (Relative to pivot)
		if (opcode == 2) {
			for (int i = 0; i < groupCount; i++) {
				int boneId = boneGroupIds[i];
				if (boneId < vertexIndicesByBone.length) {
					int[] vertexIndices = vertexIndicesByBone[boneId];
					for (int v = 0; v < vertexIndices.length; v++) {
						int vertexId = vertexIndices[v];

						// Move to local pivot space
						verticesX[vertexId] -= transformationPivotX;
						verticesY[vertexId] -= transformationPivotY;
						verticesZ[vertexId] -= transformationPivotZ;

						int angleX = (transformX & 0xff) * 8;
						int angleY = (transformY & 0xff) * 8;
						int angleZ = (transformZ & 0xff) * 8;

						// Rotate around Z axis
						if (angleZ != 0) {
							int sin = sineTable[angleZ];
							int cos = cosineTable[angleZ];
							int rotatedX = verticesY[vertexId] * sin + verticesX[vertexId] * cos >> 16;
							verticesY[vertexId] = verticesY[vertexId] * cos - verticesX[vertexId] * sin >> 16;
							verticesX[vertexId] = rotatedX;
						}

						// Rotate around X axis
						if (angleX != 0) {
							int sine = sineTable[angleX];
							int cos = cosineTable[angleX];
							int rotatedY = verticesY[vertexId] * cos - verticesZ[vertexId] * sine >> 16;
							verticesZ[vertexId] = verticesY[vertexId] * sine + verticesZ[vertexId] * cos >> 16;
							verticesY[vertexId] = rotatedY;
						}

						// Rotate around Y axis
						if (angleY != 0) {
							int sine = sineTable[angleY];
							int cos = cosineTable[angleY];
							int rotatedX = verticesZ[vertexId] * sine + verticesX[vertexId] * cos >> 16;
							verticesZ[vertexId] = verticesZ[vertexId] * cos - verticesX[vertexId] * sine >> 16;
							verticesX[vertexId] = rotatedX;
						}

						// Return to world space
						verticesX[vertexId] += transformationPivotX;
						verticesY[vertexId] += transformationPivotY;
						verticesZ[vertexId] += transformationPivotZ;
					}

				}
			}

			return;
		}

		// OPCODE 3: Scaling (Relative to pivot)
		if (opcode == 3) {
			for (int i = 0; i < groupCount; i++) {
				int boneId = boneGroupIds[i];
				if (boneId < vertexIndicesByBone.length) {
					int[] vertexIndices = vertexIndicesByBone[boneId];
					for (int v = 0; v < vertexIndices.length; v++) {
						int vertexId = vertexIndices[v];
						verticesX[vertexId] -= transformationPivotX;
						verticesY[vertexId] -= transformationPivotY;
						verticesZ[vertexId] -= transformationPivotZ;
						verticesX[vertexId] = (verticesX[vertexId] * transformX) / 128;
						verticesY[vertexId] = (verticesY[vertexId] * transformY) / 128;
						verticesZ[vertexId] = (verticesZ[vertexId] * transformZ) / 128;
						verticesX[vertexId] += transformationPivotX;
						verticesY[vertexId] += transformationPivotY;
						verticesZ[vertexId] += transformationPivotZ;
					}

				}
			}

			return;
		}

		// OPCODE 5: Alpha/Transparency modification
		if (opcode == 5 && faceIndicesByBone != null && faceTransparency != null) {
			for (int i = 0; i < groupCount; i++) {
				int boneId = boneGroupIds[i];
				if (boneId < faceIndicesByBone.length) {
					int[] vertexIndices = faceIndicesByBone[boneId];
					for (int v = 0; v < vertexIndices.length; v++) {
						int vertexId = vertexIndices[v];
						faceTransparency[vertexId] += transformX * 8;
						if (faceTransparency[vertexId] < 0)
							faceTransparency[vertexId] = 0;
						if (faceTransparency[vertexId] > 255)
							faceTransparency[vertexId] = 255;
					}
				}
			}
		}
	}

	/**
	 * Rotates the model 90 degrees around the Y-axis.
	 */
	public void rotate90Y() {
		for (int i = 0; i < verticesCount; i++) {
			int j = verticesX[i];
			verticesX[i] = verticesZ[i];
			verticesZ[i] = -j;
		}
	} //TODO MIGHT NEED A RotateY method

	/**
	 * Rotates the model around the X-axis.
	 *
	 * @param angle The angle to rotate, indexed into the sine/cosine tables.
	 */
	public void rotateX(int angle) {
		int sin = sineTable[angle];
		int cos = cosineTable[angle];

		for (int i = 0; i < verticesCount; i++) {
			int y = verticesY[i] * cos - verticesZ[i] * sin >> 16;
			verticesZ[i] = verticesY[i] * sin + verticesZ[i] * cos >> 16;
			verticesY[i] = y;
		}
	}

	public void translate(int x, int y, int z) {
		for (int i = 0; i < verticesCount; i++) {
			verticesX[i] += x;
			verticesY[i] += y;
			verticesZ[i] += z;
		}
	}

	public void replaceColor(int oldColor, int newColor) {
		for (int i = 0; i < faceCount; i++)
			if (colors[i] == oldColor)
				colors[i] = newColor;
	}

	/**
	 * Mirrors the model along the Z-axis (across the XY plane).
	 * This operation negates the Z-coordinates and reverses the face winding
	 * order to ensure normals remain pointing outward.
	 */
	public void mirrorZ() {
		// Negate the Z coordinate for all vertices
		for (int i = 0; i < verticesCount; i++)
			verticesZ[i] = -verticesZ[i];

		// Reverse the triangle winding order to prevent the model from appearing inside-out
		for (int i = 0; i < faceCount; i++) {
			int tempIndex = faceIndicesX[i];
			faceIndicesX[i] = faceIndicesZ[i];
			faceIndicesZ[i] = tempIndex;
		}
	}

	/**
	 * Scales the model along the X, Y, and Z axes.
	 *
	 * @param scaleX The scale factor for the X-axis (128 = 100%).
	 * @param scaleY The scale factor for the Y-axis (128 = 100%).
	 * @param scaleZ The scale factor for the Z-axis (128 = 100%).
	 */
	public void resizeModel(int scaleX, int scaleY, int scaleZ) {
		for (int i = 0; i < verticesCount; i++) {
			verticesX[i] = (verticesX[i] * scaleX) / 128;
			verticesY[i] = (verticesY[i] * scaleY) / 128;
			verticesZ[i] = (verticesZ[i] * scaleZ) / 128;
		}
	}

	/**
	 * Initializes the lighting and normals for the model.
	 *
	 * @param ambient          The base ambient light level.
	 * @param lightIntensity   The intensity of the light source.
	 * @param lightX           X-direction of the light.
	 * @param lightY           Y-direction of the light.
	 * @param lightZ           Z-direction of the light.
	 * @param immediateShading If true, Gouraud shading is applied immediately.
	 */
	public void initLighting(int ambient, int lightIntensity, int lightX, int lightY, int lightZ, boolean immediateShading) {
		int lightMagnitude = (int) Math.sqrt(lightX * lightX + lightY * lightY + lightZ * lightZ);
		int scaledIntensity = lightIntensity * lightMagnitude >> 8;

		// Initialize shading arrays if they don't exist
		if (faceColorsA == null) {
			faceColorsA = new int[faceCount];
			faceColorsB = new int[faceCount];
			faceColorsC = new int[faceCount];
		}

		// Initialize vertex normals if they don't exist
		if (super.normals == null) {
			super.normals = new VertexNormal[verticesCount];
			for (int i = 0; i < verticesCount; i++) {
				super.normals[i] = new VertexNormal();
			}
		}

		for (int i = 0; i < faceCount; i++) {

			int vA = faceIndicesX[i];
			int vB = faceIndicesY[i];
			int vC = faceIndicesZ[i];

			//Edges Vectors
			int edgeX1 = verticesX[vB] - verticesX[vA];
			int edgeY1 = verticesY[vB] - verticesY[vA];
			int edgeZ1 = verticesZ[vB] - verticesZ[vA];

			int edgeX2 = verticesX[vC] - verticesX[vA];
			int edgeY2 = verticesY[vC] - verticesY[vA];
			int edgeZ2 = verticesZ[vC] - verticesZ[vA];

			int normalX = edgeY1 * edgeZ2 - edgeY2 * edgeZ1;
			int normalY = edgeZ1 * edgeX2 - edgeZ2 * edgeX1;
			int normalZ = edgeX1 * edgeY2 - edgeX2 * edgeY1;

			//Scale down to prevent overflow (The Scaling Guard)
			while(normalX > MAX_NORMAL_COMPONENT || normalY > MAX_NORMAL_COMPONENT || normalZ > MAX_NORMAL_COMPONENT ||
				  normalX < -MAX_NORMAL_COMPONENT || normalY < -MAX_NORMAL_COMPONENT || normalZ < -MAX_NORMAL_COMPONENT) {
				normalX >>= 1;
				normalY >>= 1;
				normalZ >>= 1;
			}

			int magnitude = (int) Math.sqrt(normalX * normalX + normalY * normalY + normalZ * normalZ); //TODO add + 0.5

			if (magnitude <= 0) {
				magnitude = 1;
			}

			// Normalize the vector components to the engine's standard scale (256)
			normalX = (normalX * 256) / magnitude;
			normalY = (normalY * 256) / magnitude;
			normalZ = (normalZ * 256) / magnitude;

			boolean isSmoothShaded = (faceRenderTypes == null) || (faceRenderTypes[i] & RENDER_TYPE_FLAT_SHADING) == 0;

			if(isSmoothShaded) {
				accumulateVertexNormal(vA, normalX, normalY, normalZ);
				accumulateVertexNormal(vB, normalX, normalY, normalZ);
				accumulateVertexNormal(vC, normalX, normalY, normalZ);
			} else {
				int lightDotProduct = (lightX * normalX + lightY * normalY + lightZ * normalZ);
				int faceIntensity = ambient + lightDotProduct / (scaledIntensity + scaledIntensity / 2);
				faceColorsA[i] = applyLightToColor(colors[i], faceIntensity, faceRenderTypes[i]);
			}
		}

		if (immediateShading) {
			calculateShading(ambient, scaledIntensity, lightX, lightY, lightZ);
			calculateRadius();
		} else {
			vertexNormalsTable = new VertexNormal[verticesCount];
			for (int i = 0; i < verticesCount; i++) {
				VertexNormal source = super.normals[i];
				VertexNormal destination = vertexNormalsTable[i] = new VertexNormal();
				destination.x = source.x;
				destination.y = source.y;
				destination.z = source.z;
				destination.magnitude = source.magnitude;
			}

			lightingParameters = (ambient << 16) | (scaledIntensity & 0xffff);

			// Calculate the full Axis-Aligned Bounding Box (AABB)
			calculateAABB();
		}
	}

	/**
	 * Adds a face normal to a vertex's accumulated normal.
	 * This is used to calculate the average normal for Gouraud (smooth) shading.
	 */
	private void accumulateVertexNormal(int vertexIndex, int nx, int ny, int nz) {
		VertexNormal vNorm = super.normals[vertexIndex];
		vNorm.x += nx;
		vNorm.y += ny;
		vNorm.z += nz;
		vNorm.magnitude++; // Tracks how many faces contribute to this vertex
	}

	/**
	 * Re-calculates the shading of the model using stored ambient and intensity values
	 * combined with a new light source direction.
	 *
	 * @param lightX The X-coordinate of the light source.
	 * @param lightY The Y-coordinate of the light source.
	 * @param lightZ The Z-coordinate of the light source.
	 */
	public void reapplyLighting(int lightX, int lightY, int lightZ) {
		// High 16 bits: Ambient, Low 16 bits: Light Magnitude
		int ambient = lightingParameters >> 16;
		int magnitude = (lightingParameters << 16) >> 16;

		calculateShading(ambient, magnitude, lightX, lightY, lightZ);
	}

	/**
	 * Calculates the Gouraud shading for the model based on a light source vector.
	 *
	 * @param ambient   The base ambient light level.
	 * @param magnitude The intensity/magnitude of the light source.
	 * @param lightX    X-component of the light direction.
	 * @param lightY    Y-component of the light direction.
	 * @param lightZ    Z-component of the light direction.
	 */
	public void calculateShading(int ambient, int magnitude, int lightX, int lightY, int lightZ) {
		for (int face = 0; face < faceCount; face++) {

			// Get the vertex indices for this face
			int vA = faceIndicesX[face];
			int vB = faceIndicesY[face];
			int vC = faceIndicesZ[face];

			// Determine if this face has specific render properties (like flat shading or textures)
			int renderType = (faceRenderTypes == null) ? 0 : faceRenderTypes[face];

			faceColorsA[face] = calculateVertexLight(vA, ambient, magnitude, lightX, lightY, lightZ, colors[face], renderType);
			faceColorsB[face] = calculateVertexLight(vB, ambient, magnitude, lightX, lightY, lightZ, colors[face], renderType);
			faceColorsC[face] = calculateVertexLight(vC, ambient, magnitude, lightX, lightY, lightZ, colors[face], renderType);

		}

		// Cleanup resources no longer needed after shading is baked into vertex colors
		super.normals = null;
		vertexNormalsTable = null;
		vertexBoneIds = null;
		faceBoneIds = null;
		if (faceRenderTypes != null) {
			for (int i = 0; i < faceCount; i++)
				if ((faceRenderTypes[i] & 2) == 2)
					return;
		}
		colors = null;
	}

	/**
	 * Helper to calculate the light intensity for a specific vertex using the Dot Product.
	 */
	private int calculateVertexLight(int vertexIndex, int ambient, int magnitude, int lx, int ly, int lz, int color, int type) {
		VertexNormal normal = super.normals[vertexIndex];

		// Calculate the divisor for the lighting equation
		int divisor = magnitude * normal.magnitude;
		int intensity;

		if (divisor != 0) {
			// Calculate the dot product between the light vector and the vertex normal
			int dotProduct = (lx * normal.x + ly * normal.y + lz * normal.z);
			intensity = ambient + (dotProduct / divisor);
		} else {
			// Fallback: If light magnitude or normal magnitude is zero,
			// the vertex only receives ambient light.
			intensity = ambient;
		}

		// method597 likely applies the intensity to the HSL/RGB color space
		return applyLightToColor(color, intensity, type);
	}

	/**
	 * Applies calculated light intensity to a packed HSL color.
	 *
	 * @param packedHsl      The base color (High bits: Hue/Sat, Low 7 bits: Lightness).
	 * @param lightIntensity The intensity of light calculated from the 3D scene (0-127).
	 * @param renderConfig   Bitmask for face properties (Bit 2: Inversion/Grayscale).
	 * @return The light-adjusted packed HSL color.
	 */
	public static int applyLightToColor(int packedHsl, int lightIntensity, int renderConfig) {
		// Check for special "Inversion" or Grayscale flag
		if ((renderConfig & 0x2) == 2) {
			if (lightIntensity < 0) {
				lightIntensity = 0;
			} else if (lightIntensity > 127) {
				lightIntensity = 127;
			}
			return 127 - lightIntensity;
		}

		// Extract the original lightness (bottom 7 bits)
		int baseLightness = packedHsl & 0x7f;

		// Apply lighting: (Intensity * BaseLightness) / 128
		int finalLightness = (lightIntensity * baseLightness) >> 7;

		// Clamp values to keep them within the visible 2-126 range
		if (finalLightness < 2) {
			finalLightness = 2;
		} else if (finalLightness > 126) {
			finalLightness = 126;
		}

		// Recombine the original Hue/Sat with the new Lightness
		return (packedHsl & 0xff80) | finalLightness;
	}

	/**
	 * Projects the model's vertices from local space to 24-bit fixed-point screen space.
	 *
	 * @param pitch      Rotation around the X-axis.
	 * @param yaw        Rotation around the Y-axis.
	 * @param roll       Rotation around the Z-axis.
	 * @param viewPitch  The pitch/tilt of the camera view.
	 * @param offsetX    Translation along the X-axis.
	 * @param offsetY    Translation along the Y-axis.
	 * @param offsetZ    Translation along the Z-axis.
	 */
	public void viewportTransform(int pitch, int yaw, int roll, int viewPitch, int offsetX, int offsetY, int offsetZ) {
		int centerX = ThreeDimensionalCanvas.halfParentWidth;
		int centerY = ThreeDimensionalCanvas.halfParentHeight;

		// Pre-calculate Trigonometry for the rotation matrix
		int sinPitch = sineTable[pitch];
		int cosPitch = cosineTable[pitch];
		int sinYaw = sineTable[yaw];
		int cosYaw = cosineTable[yaw];
		int sinRoll = sineTable[roll];
		int cosRoll = cosineTable[roll];
		int sinView = sineTable[viewPitch];
		int cosView = cosineTable[viewPitch];

		// Calculate a Z-depth offset based on camera height and tilt
		int viewZOffset = offsetY * sinView + offsetZ * cosView >> 16;

		for (int v = 0; v < verticesCount; v++) {
			int x = verticesX[v];
			int y = verticesY[v];
			int z = verticesZ[v];

			// Apply Roll (Rotation around Z-axis)
			if (roll != 0) {
				int tempX = y * sinRoll + x * cosRoll >> 16;
				y = y * cosRoll - x * sinRoll >> 16;
				x = tempX;
			}

			// Apply Pitch (Rotation around X-axis)
			if (pitch != 0) {
				int tempY = y * cosPitch - z * sinPitch >> 16;
				z = y * sinPitch + z * cosPitch >> 16;
				y = tempY;
			}

			// Apply Yaw (Rotation around Y-axis)
			if (yaw != 0) {
				int tempX = z * sinYaw + x * cosYaw >> 16;
				z = z * cosYaw - x * sinYaw >> 16;
				x = tempX;
			}

			// Translation (Move to world position)
			x += offsetX;
			y += offsetY;
			z += offsetZ;

			// Final Camera Transformation (Apply view tilt)
			int transformedY = y * cosView - z * sinView >> 16;
			z = y * sinView + z * cosView >> 16;
			y = transformedY;

			// Perspective Projection
			// Formula: screenPos = center + (worldPos * focalLength) / depth
			// The value 512 (1 << 9) represents the focal length/field of view.
			projectedZ[v] = z - viewZOffset;
			projectedX[v] = centerX + (x << 9) / z;
			projectedY[v] = centerY + (y << 9) / z;
			if (textureVertexCount > 0) {
				cameraX[v] = x;
				cameraY[v] = y;
				cameraZ[v] = z;
			}
		}

		try {
			processFaces(false, false, 0);
		} catch (Exception _ex) {
			// Catching exceptions here prevents a single malformed model
			// from crashing the entire frame's render loop.
		}
	}

	/**
	 * Renders the model as an entity within the game world, including frustum culling
	 * and mouse-hover detection.
	 *
	 * @param yaw           The model's local rotation around the Y-axis.
	 * @param sinViewPitch  Sine of the camera's pitch.
	 * @param cosViewPitch  Cosine of the camera's pitch.
	 * @param sinViewYaw    Sine of the camera's yaw.
	 * @param cosViewYaw    Cosine of the camera's yaw.
	 * @param relativeX     World X-translation relative to the camera.
	 * @param relativeY     World Y-translation relative to the camera.
	 * @param relativeZ     World Z-translation relative to the camera.
	 * @param modelId       Unique ID used for mouse-picking/interaction.
	 */
	@Override
	public void render(int yaw, int sinViewPitch, int cosViewPitch, int sinViewYaw, int cosViewYaw, int relativeX, int relativeY, int relativeZ, int modelId) {

		//Initial View Transformation (Camera Space Calculation)
		int transformedZ = relativeZ * cosViewYaw - relativeX * sinViewYaw >> 16;
		int depthZ = relativeY * sinViewPitch + transformedZ * cosViewPitch >> 16;
		int radiusZ = modelRadius * cosViewPitch >> 16;
		int maxZ = depthZ + radiusZ;

		// Frustum Culling (Z-Axis)
		// If the entire model is behind the near plane (50) or too far away, stop.
		if (maxZ <= 50 || depthZ >= 3500)
			return;

		int transformedX = relativeZ * sinViewYaw + relativeX * cosViewYaw >> 16;
		int leftLimit = transformedX - modelRadius << 9;

		// Frustum Culling (Horizontal/X-Axis)
		if (leftLimit / maxZ >= Drawable.halfWidthX)
			return;

		int rightLimit = transformedX + modelRadius << 9;

		if (rightLimit / maxZ <= -Drawable.halfWidthX)
			return;

		int transformedY = relativeY * cosViewPitch - transformedZ * sinViewPitch >> 16;
		int radiusY = modelRadius * sinViewPitch >> 16;
		int topLimit = transformedY + radiusY << 9;

		// Frustum Culling (Vertical/Y-Axis)
		if (topLimit / maxZ <= -Drawable.halfHeightY)
			return;

		int bottomLimit = radiusY + (super.height * cosViewPitch >> 16);
		int bottomBound = transformedY - bottomLimit << 9;

		if (bottomBound / maxZ >= Drawable.halfHeightY)
			return;

		// Broad-Phase Mouse Picking
		// Check if the mouse cursor is roughly within the screen-space bounding box
		boolean isMouseOver = false;
		boolean needsClipping = depthZ - (radiusZ + (super.height * sinViewPitch >> 16)) <= 50;

		if (modelId > 0 && isPickingEnabled) {
			int minZ = depthZ - radiusZ;
			if (minZ <= 50)
				minZ = 50;

			// Perspective-correct bounds for mouse check
			if (transformedX > 0) {
				leftLimit /= maxZ;
				rightLimit /= minZ;
			} else {
				rightLimit /= maxZ;
				leftLimit /= minZ;
			}

			if (transformedY > 0) {
				bottomBound /= maxZ;
				topLimit /= minZ;
			} else {
				topLimit /= maxZ;
				bottomBound /= minZ;
			}

			int mouseRelX = mouseX - ThreeDimensionalCanvas.halfParentWidth;
			int mouseRelY = mouseY - ThreeDimensionalCanvas.halfParentHeight;
			if (mouseRelX > leftLimit && mouseRelX < rightLimit && mouseRelY > bottomBound && mouseRelY < topLimit)
				if (isPriorityPicking)
					hoveredModels[hoveredCount++] = modelId;
				else
					isMouseOver = true;
		}

		// Vertex Transformation Loop
		int screenCenterX = ThreeDimensionalCanvas.halfParentWidth;
		int screenCenterY = ThreeDimensionalCanvas.halfParentHeight;
		int sinYaw = 0;
		int cosYaw = 0;

		if (yaw != 0) {
			sinYaw = sineTable[yaw];
			cosYaw = cosineTable[yaw];
		}

		for (int v = 0; v < verticesCount; v++) {
			int vX = verticesX[v];
			int vY = verticesY[v];
			int vZ = verticesZ[v];

			if (yaw != 0) {
				int rotateX = vZ * sinYaw + vX * cosYaw >> 16;
				vZ = vZ * cosYaw - vX * sinYaw >> 16;
				vX = rotateX;
			}

			// Apply World Position + Camera Rotation
			vX += relativeX;
			vY += relativeY;
			vZ += relativeZ;


			int xView = vZ * sinViewYaw + vX * cosViewYaw >> 16;
			vZ = vZ * cosViewYaw - vX * sinViewYaw >> 16;
			vX = xView;

			int yView = vY * cosViewPitch - vZ * sinViewPitch >> 16;
			vZ = vY * sinViewPitch + vZ * cosViewPitch >> 16;
			vY = yView;

			// Apply Projection
			projectedZ[v] = vZ - depthZ;

			if (vZ >= 50) {
				projectedX[v] = screenCenterX + (vX << 9) / vZ;
				projectedY[v] = screenCenterY + (vY << 9) / vZ;
			} else {
				projectedX[v] = -5000;
				needsClipping = true;
			}
			if (needsClipping || textureVertexCount > 0) {
				cameraX[v] = vX;
				cameraY[v] = vY;
				cameraZ[v] = vZ;
			}
		}

		// Rasterization and Face Processing
		try {
			processFaces(needsClipping, isMouseOver, modelId);
		} catch (Exception _ex) {
			// Catching exceptions here prevents a single malformed model
			// from crashing the entire frame's render loop.
			// In a production environment, consider logging this exception for debugging.
		}
	}

	/**
	 * Processes, sorts, and dispatches faces for rendering.
	 *
	 * <p>This method implements the core visibility pipeline:
	 * 1. Backface Culling: Discards triangles facing away from the camera.
	 * 2. Mouse Picking: Checks for per-face interaction.
	 * 3. Painter's Algorithm: Sorts faces into depth bins (0 to totalDepthSortingRange).
	 * 4. Priority Layering: Handles complex Z-ordering overrides (12 priority levels).</p>
	 *
	 * @param needsClipping   True if the model is near the camera's near-plane.
	 * @param isMouseOver     True if the mouse is within the model's broad bounding box.
	 * @param modelId         The unique ID for interaction events.
	 */
	public void processFaces(boolean needsClipping, boolean isMouseOver, int modelId) {
		//TODO split this method into smaller methods for each stage of the pipeline (culling, picking, sorting, rendering)
		// Clear the depth bin counters
		for (int i = 0; i < totalDepthSortingRange; i++) {
			faceDepthCounts[i] = 0;
		}

		// Culling, Picking, and Depth Sorting Loop
		for (int face = 0; face < faceCount; face++) {

			// Skip hidden faces
			if (faceRenderTypes != null && faceRenderTypes[face] == -1) {
				continue;
			}

			int vA = faceIndicesX[face];
			int vB = faceIndicesY[face];
			int vC = faceIndicesZ[face];

			int xA = projectedX[vA];
			int xB = projectedX[vB];
			int xC = projectedX[vC];

			// Handle Near-Plane Clipping logic
			// -5000 is the sentinel value for vertices projected behind the camera
			if (needsClipping && (xA == -5000 || xB == -5000 || xC == -5000)) {
				faceNeedsClipping[face] = true;

				// Calculate average Z depth and add to bin
				int avgZ = (projectedZ[vA] + projectedZ[vB] + projectedZ[vC]) / 3 + modelBoundingSphere;
				faceDepthBins[avgZ][faceDepthCounts[avgZ]++] = face;
			} else {
				// Narrow-Phase Mouse Picking: Check if mouse is inside this specific triangle
				if (isMouseOver && isPointInFaceBounds(mouseX, mouseY, projectedY[vA], projectedY[vB], projectedY[vC], xA, xB, xC)) {
					hoveredModels[hoveredCount++] = modelId;
					isMouseOver = false;
				}

				// 2D Backface Culling (Cross Product check)
				// If the cross product of two edges is positive, the face is pointing towards the camera.
				if ((xA - xB) * (projectedY[vC] - projectedY[vB]) - (projectedY[vA] - projectedY[vB]) * (xC - xB) > 0) {
					faceNeedsClipping[face] = false;

					// Viewport Frustum Check (Horizontal Clipping)
					faceIsOffScreen[face] = xA < 0 || xB < 0 || xC < 0 || xA > Drawable.lastPixelX || xB > Drawable.lastPixelX || xC > Drawable.lastPixelX;

					// Assign to depth bin based on average projected Z
					int avgZ = (projectedZ[vA] + projectedZ[vB] + projectedZ[vC]) / 3 + modelBoundingSphere;
					faceDepthBins[avgZ][faceDepthCounts[avgZ]++] = face;
				}
			}
		}

		// --- CASE 1: Rendering without priority layers ---
		if (facePriorities == null) {
			for (int depth = totalDepthSortingRange - 1; depth >= 0; depth--) {
				int countAtDepth = faceDepthCounts[depth];
				if (countAtDepth > 0) {
					int[] facesAtDepth = faceDepthBins[depth];
					for (int faceIndex = 0; faceIndex < countAtDepth; faceIndex++)
						drawFace(facesAtDepth[faceIndex]);
				}
			}
			return;
		}

		// --- CASE 2: Rendering with 12 priority layers ---
		for (int priority = 0; priority < 12; priority++) {
			priorityCounts[priority] = 0;
			priorityAverages[priority] = 0;
		}

		// Sort depth bins into priority bins
		for (int depth = totalDepthSortingRange - 1; depth >= 0; depth--) {
			int countAtDepth = faceDepthCounts[depth];
			if (countAtDepth > 0) {
				int[] facesAtDepth = faceDepthBins[depth];
				for (int faceIndex = 0; faceIndex < countAtDepth; faceIndex++) {
					int faceId = facesAtDepth[faceIndex];
					int priority = facePriorities[faceId];
					int priorityBinIndex = priorityCounts[priority]++;
					priorityBins[priority][priorityBinIndex] = faceId;

					// Track depth averages for specific tiers to determine intersection points
					if (priority < 10)
						priorityAverages[priority] += depth;
					else if (priority == 10)
						priorityDepthX[priorityBinIndex] = depth;
					else
						priorityDepthY[priorityBinIndex] = depth;
				}

			}
		}

		// Calculate depth thresholds for specific priority tiers
		int avgDepthTier1and2 = 0;
		if (priorityCounts[1] > 0 || priorityCounts[2] > 0) {
			avgDepthTier1and2 = (priorityAverages[1] + priorityAverages[2]) / (priorityCounts[1] + priorityCounts[2]);
		}

		int avgDepthTier3and4 = 0;
		if (priorityCounts[3] > 0 || priorityCounts[4] > 0) {
			avgDepthTier3and4 = (priorityAverages[3] + priorityAverages[4]) / (priorityCounts[3] + priorityCounts[4]);
		}

		int avgDepthTier6and8 = 0;
		if (priorityCounts[6] > 0 || priorityCounts[8] > 0) {
			avgDepthTier6and8 = (priorityAverages[6] + priorityAverages[8]) / (priorityCounts[6] + priorityCounts[8]);
		}

		// Initialize pointers for high-priority (tier 10/11) rendering
		int highPriorityPointer = 0;
		int highPriorityLimit = priorityCounts[10];
		int[] highPriorityFaceBin = priorityBins[10];
		int[] highPriorityDepthBin = priorityDepthX;
		// If Priority 10 is empty, move immediately to Priority 11
		if (highPriorityPointer == highPriorityLimit) {
			highPriorityPointer = 0;
			highPriorityLimit = priorityCounts[11];
			highPriorityFaceBin = priorityBins[11];
			highPriorityDepthBin = priorityDepthY;
		}

		int currentHighPriorityDepth;
		if (highPriorityPointer < highPriorityLimit) {
			currentHighPriorityDepth = highPriorityDepthBin[highPriorityPointer];
		}
		else {
			currentHighPriorityDepth = -1000;
		}

		// Main Rendering Loop: Priority Levels 0-9
		for (int priorityLevel = 0; priorityLevel < 10; priorityLevel++) {
			while (priorityLevel == 0 && currentHighPriorityDepth > avgDepthTier1and2) {
				drawFace(highPriorityFaceBin[highPriorityPointer++]);
				if (highPriorityPointer == highPriorityLimit && highPriorityFaceBin != priorityBins[11]) {
					highPriorityPointer = 0;
					highPriorityLimit = priorityCounts[11];
					highPriorityFaceBin = priorityBins[11];
					highPriorityDepthBin = priorityDepthY;
				}
				if (highPriorityPointer < highPriorityLimit)
					currentHighPriorityDepth = highPriorityDepthBin[highPriorityPointer];
				else
					currentHighPriorityDepth = -1000;
			}

			while (priorityLevel == 3 && currentHighPriorityDepth > avgDepthTier3and4) {
				drawFace(highPriorityFaceBin[highPriorityPointer++]);
				if (highPriorityPointer == highPriorityLimit && highPriorityFaceBin != priorityBins[11]) {
					highPriorityPointer = 0;
					highPriorityLimit = priorityCounts[11];
					highPriorityFaceBin = priorityBins[11];
					highPriorityDepthBin = priorityDepthY;
				}
				if (highPriorityPointer < highPriorityLimit)
					currentHighPriorityDepth = highPriorityDepthBin[highPriorityPointer];
				else
					currentHighPriorityDepth = -1000;
			}

			while (priorityLevel == 5 && currentHighPriorityDepth > avgDepthTier6and8) {
				drawFace(highPriorityFaceBin[highPriorityPointer++]);
				if (highPriorityPointer == highPriorityLimit && highPriorityFaceBin != priorityBins[11]) {
					highPriorityPointer = 0;
					highPriorityLimit = priorityCounts[11];
					highPriorityFaceBin = priorityBins[11];
					highPriorityDepthBin = priorityDepthY;
				}
				if (highPriorityPointer < highPriorityLimit)
					currentHighPriorityDepth = highPriorityDepthBin[highPriorityPointer];
				else
					currentHighPriorityDepth = -1000;
			}

			// Draw faces for the current standard priority level (0-9)
			int facesInLevelCount = priorityCounts[priorityLevel];
			int[] facesInLevel = priorityBins[priorityLevel];
			for (int i = 0; i < facesInLevelCount; i++) {
				drawFace(facesInLevel[i]);
			}
		}

		// Flush remaining high-priority faces (10 and 11)
		while (currentHighPriorityDepth != -1000) {
			drawFace(highPriorityFaceBin[highPriorityPointer++]);
			if (highPriorityPointer == highPriorityLimit && highPriorityFaceBin != priorityBins[11]) {
				highPriorityPointer = 0;
				highPriorityFaceBin = priorityBins[11];
				highPriorityLimit = priorityCounts[11];
				highPriorityDepthBin = priorityDepthY;
			}
			if (highPriorityPointer < highPriorityLimit)
				currentHighPriorityDepth = highPriorityDepthBin[highPriorityPointer];
			else
				currentHighPriorityDepth = -1000;
		}
	}

	/**
	 * Dispatches a face to the rasterizer based on its render configuration.
	 *
	 * @param faceId The index of the face to be drawn.
	 */
	public void drawFace(int faceId) {
		// Handle Near-Plane Clipping
		if (faceNeedsClipping[faceId]) {
			clipAndDrawFace(faceId);
			return;
		}

		// Fetch Vertex Indices
		int vA = faceIndicesX[faceId];
		int vB = faceIndicesY[faceId];
		int vC = faceIndicesZ[faceId];

		// Sync Global Rasterizer State
		ThreeDimensionalCanvas.hClip = faceIsOffScreen[faceId];

		if (faceTransparency == null)
			ThreeDimensionalCanvas.anInt1531 = 0;
		else
			ThreeDimensionalCanvas.anInt1531 = faceTransparency[faceId];

		// Determine Render Mode (Bits 0-1)
		int renderMode = (faceRenderTypes == null) ? 0 : faceRenderTypes[faceId] & 3;

		switch(renderMode) {
			case 0: // Gouraud (Smooth) Shading
				ThreeDimensionalCanvas.gouraudTriangle(projectedY[vA], projectedY[vB], projectedY[vC],
						projectedX[vA], projectedX[vB], projectedX[vC], faceColorsA[faceId], faceColorsB[faceId],
						faceColorsC[faceId]);
				break;
			case 1: // Flat Shading
				ThreeDimensionalCanvas.flatTriangle(projectedY[vA], projectedY[vB], projectedY[vC],
						projectedX[vA], projectedX[vB], projectedX[vC], colorLookupTable[faceColorsA[faceId]]);
				break;
			case 2: case 3:
				// Delegate to a helper method for textured faces, as they share common parameters
				// The textureIndex variable is now scoped within drawTexturedFace
				drawTexturedFace(faceId, vA, vB, vC, renderMode);
				break;
		}
	}

	/**
	 * Internal helper to handle the complex parameter list for textured triangles.
	 * This method is called by drawFace when the renderMode indicates a textured face.
	 */
	private void drawTexturedFace(int faceId, int vA, int vB, int vC, int mode) {
		int textureIndex = faceRenderTypes[faceId] >> 2;
		int tA = textureVertexIndicesA[textureIndex];
		int tB = textureVertexIndicesB[textureIndex];
		int tC = textureVertexIndicesC[textureIndex];

		int colorA = faceColorsA[faceId];
		int colorB = (mode == 3) ? colorA : faceColorsB[faceId]; // Handles the difference between mode 2 and 3
		int colorC = (mode == 3) ? colorA : faceColorsC[faceId]; // Handles the difference between mode 2 and 3

		ThreeDimensionalCanvas.textureTriangle(projectedY[vA], projectedY[vB], projectedY[vC],
				projectedX[vA], projectedX[vB], projectedX[vC], colorA, colorB,
				colorC, cameraX[tA], cameraX[tB], cameraX[tC], cameraY[tA],
				cameraY[tB], cameraY[tC], cameraZ[tA], cameraZ[tB], cameraZ[tC],
				colors[faceId]);
	}

	/**
	 * Clips a face against the near-plane (Z=50) and dispatches it to the rasterizer.
	 *
	 * <p>When a face crosses the near-plane, this method calculates the intersection
	 * points to create a new visible polygon. If the clipped result is a triangle,
	 * it is drawn directly. If it results in a quadrilateral, it is tessellated into
	 * two triangles.</p>
	 *
	 * @param faceId The index of the face to clip and draw.
	 */
	public void clipAndDrawFace(int faceId) {
		int centerX = ThreeDimensionalCanvas.halfParentWidth;
		int centerY = ThreeDimensionalCanvas.halfParentHeight;
		int clippedCount = 0;

		int vA = faceIndicesX[faceId];
		int vB = faceIndicesY[faceId];
		int vC = faceIndicesZ[faceId];

		int zA = cameraZ[vA];
		int zB = cameraZ[vB];
		int zC = cameraZ[vC];

		final int NEAR_PLANE = 50;

		// --- Vertex A Clipping Logic ---
		if (zA >= NEAR_PLANE) {
			clippedProjectedX[clippedCount] = projectedX[vA];
			clippedProjectedY[clippedCount] = projectedY[vA];
			clippedVertexColors[clippedCount++] = faceColorsA[faceId];
		} else {
			int xA = cameraX[vA];
			int yA = cameraY[vA];
			int colorA = faceColorsA[faceId];

			if (zC >= NEAR_PLANE) { //Edge A-C crosses near plane
				int lerpRatio = (NEAR_PLANE - zA) * reciprocalTable[zC - zA];
				clippedProjectedX[clippedCount] = centerX + (xA + ((cameraX[vC] - xA) * lerpRatio >> 16) << 9) / NEAR_PLANE;
				clippedProjectedY[clippedCount] = centerY + (yA + ((cameraY[vC] - yA) * lerpRatio >> 16) << 9) / NEAR_PLANE;
				clippedVertexColors[clippedCount++] = colorA + ((faceColorsC[faceId] - colorA) * lerpRatio >> 16);
			}
			if (zB >= NEAR_PLANE) {
				int lerpRatio = (NEAR_PLANE - zA) * reciprocalTable[zB - zA];
				clippedProjectedX[clippedCount] = centerX + (xA + ((cameraX[vB] - xA) * lerpRatio >> 16) << 9) / NEAR_PLANE;
				clippedProjectedY[clippedCount] = centerY + (yA + ((cameraY[vB] - yA) * lerpRatio >> 16) << 9) / NEAR_PLANE;
				clippedVertexColors[clippedCount++] = colorA + ((faceColorsB[faceId] - colorA) * lerpRatio >> 16);
			}
		}

		// --- Vertex B Clipping Logic ---
		if (zB >= NEAR_PLANE) {
			clippedProjectedX[clippedCount] = projectedX[vB];
			clippedProjectedY[clippedCount] = projectedY[vB];
			clippedVertexColors[clippedCount++] = faceColorsB[faceId];
		} else {
			int xB = cameraX[vB];
			int yB = cameraY[vB];
			int colorB = faceColorsB[faceId];
			if (zA >= NEAR_PLANE) {
				int lerpRatio = (NEAR_PLANE - zB) * reciprocalTable[zA - zB];
				clippedProjectedX[clippedCount] = centerX + (xB + ((cameraX[vA] - xB) * lerpRatio >> 16) << 9) / NEAR_PLANE;
				clippedProjectedY[clippedCount] = centerY + (yB + ((cameraY[vA] - yB) * lerpRatio >> 16) << 9) / NEAR_PLANE;
				clippedVertexColors[clippedCount++] = colorB + ((faceColorsA[faceId] - colorB) * lerpRatio >> 16);
			}
			if (zC >= NEAR_PLANE) {
				int lerpRatio = (NEAR_PLANE - zB) * reciprocalTable[zC - zB];
				clippedProjectedX[clippedCount] = centerX + (xB + ((cameraX[vC] - xB) * lerpRatio >> 16) << 9) / NEAR_PLANE;
				clippedProjectedY[clippedCount] = centerY + (yB + ((cameraY[vC] - yB) * lerpRatio >> 16) << 9) / NEAR_PLANE;
				clippedVertexColors[clippedCount++] = colorB + ((faceColorsC[faceId] - colorB) * lerpRatio >> 16);
			}
		}

		// --- Vertex C Clipping Logic ---
		if (zC >= NEAR_PLANE) {
			clippedProjectedX[clippedCount] = projectedX[vC];
			clippedProjectedY[clippedCount] = projectedY[vC];
			clippedVertexColors[clippedCount++] = faceColorsC[faceId];
		} else {
			int xC = cameraX[vC];
			int yC = cameraY[vC];
			int colorC = faceColorsC[faceId];
			if (zB >= NEAR_PLANE) {
				int lerpRatio = (NEAR_PLANE - zC) * reciprocalTable[zB - zC];
				clippedProjectedX[clippedCount] = centerX + (xC + ((cameraX[vB] - xC) * lerpRatio >> 16) << 9) / NEAR_PLANE;
				clippedProjectedY[clippedCount] = centerY + (yC + ((cameraY[vB] - yC) * lerpRatio >> 16) << 9) / NEAR_PLANE;
				clippedVertexColors[clippedCount++] = colorC + ((faceColorsB[faceId] - colorC) * lerpRatio >> 16);
			}
			if (zA >= NEAR_PLANE) {
				int lerpRatio = (NEAR_PLANE - zC) * reciprocalTable[zA - zC];
				clippedProjectedX[clippedCount] = centerX + (xC + ((cameraX[vA] - xC) * lerpRatio >> 16) << 9) / 50;
				clippedProjectedY[clippedCount] = centerY + (yC + ((cameraY[vA] - yC) * lerpRatio >> 16) << 9) / 50;
				clippedVertexColors[clippedCount++] = colorC + ((faceColorsA[faceId] - colorC) * lerpRatio >> 16);
			}
		}

		// Fail-safe: If after clipping we have less than 3 vertices, we cannot form a valid polygon to render.
		if (clippedCount < 3) return;

		int x1 = clippedProjectedX[0], x2 = clippedProjectedX[1], x3 = clippedProjectedX[2];
		int y1 = clippedProjectedY[0], y2 = clippedProjectedY[1], y3 = clippedProjectedY[2];

		// Backface Culling Check (Cross Product)
		if ((x1 - x2) * (y3 - y2) - (y1 - y2) * (x3 - x2) > 0) {

			// Push rendering state to Canvas
			ThreeDimensionalCanvas.hClip = isOffScreen(clippedCount);
			int renderMode = (faceRenderTypes == null) ? 0 : (faceRenderTypes[faceId] & 3);

			// Draw the first triangle
			renderClippedTriangle(faceId, renderMode, 0, 1, 2);

			// If it's a quad, draw the second triangle (0, 2, 3)
			if (clippedCount == 4) {
				renderClippedTriangle(faceId, renderMode, 0, 2, 3);
			}
		}
	}

	/**
	 * Checks if any vertices in the clipped polygon are off-screen to enable rasterizer clipping.
	 */
	private boolean isOffScreen(int count) {
		for (int i = 0; i < count; i++) {
			if (clippedProjectedX[i] < 0 || clippedProjectedX[i] > Drawable.lastPixelX) return true;
		}
		return false;
	}

	/**
	 * Helper to dispatch drawing for clipped polygon segments.
	 */
	private void renderClippedTriangle(int faceId, int mode, int idx1, int idx2, int idx3) {
		int xA = clippedProjectedX[idx1], xB = clippedProjectedX[idx2], xC = clippedProjectedX[idx3];
		int yA = clippedProjectedY[idx1], yB = clippedProjectedY[idx2], yC = clippedProjectedY[idx3];
		int cA = clippedVertexColors[idx1], cB = clippedVertexColors[idx2], cC = clippedVertexColors[idx3];

		switch(mode) {
			case 0:  // Gouraud triangle
				ThreeDimensionalCanvas.gouraudTriangle(yA, yB, yC, xA, xB, xC, cA, cB, cC);
				break;
			case 1: // Flat triangle
				ThreeDimensionalCanvas.flatTriangle(yA, yB, yC, xA, xB, xC, colorLookupTable[faceColorsA[faceId]]);
				break;
			case 2: // Textured
			case 3: // Textured Flat
				int textureIndex = faceRenderTypes[faceId] >> 2;
				int texA = textureVertexIndicesA[textureIndex];
				int texB = textureVertexIndicesB[textureIndex];
				int texC = textureVertexIndicesC[textureIndex];

				// For Flat Textured, we override corners with the primary color
				int colorA = (mode == 3) ? faceColorsA[faceId] : cA;
				int colorB = (mode == 3) ? faceColorsA[faceId] : cB;
				int colorC = (mode == 3) ? faceColorsA[faceId] : cC;

				ThreeDimensionalCanvas.textureTriangle(yA, yB, yC, xA, xB, xC, colorA, colorB,
						colorC, cameraX[texA], cameraX[texB], cameraX[texC],
						cameraY[texA], cameraY[texB], cameraY[texC], cameraZ[texA],
						cameraZ[texB], cameraZ[texC], colors[faceId]);
				break;

		}
	}

	/**
	 * Performs a fast Axis-Aligned Bounding Box (AABB) intersection check
	 * to determine if a point (mouse) is within the screen-space extents of a face.
	 *
	 * @param mouseX The current X-position of the mouse.
	 * @param mouseY The current Y-position of the mouse.
	 * @param v1Y    Projected Y-coordinate of the first vertex.
	 * @param v2Y    Projected Y-coordinate of the second vertex.
	 * @param v3Y    Projected Y-coordinate of the third vertex.
	 * @param v1X    Projected X-coordinate of the first vertex.
	 * @param v2X    Projected X-coordinate of the second vertex.
	 * @param v3X    Projected X-coordinate of the third vertex.
	 * @return True if the point is within the face's bounding box; otherwise false.
	 */
	public boolean isPointInFaceBounds(int mouseX, int mouseY, int v1Y, int v2Y, int v3Y, int v1X, int v2X, int v3X) {
		if (mouseY < v1Y && mouseY < v2Y && mouseY < v3Y)
			return false;
		if (mouseY > v1Y && mouseY > v2Y && mouseY > v3Y)
			return false;
		if (mouseX < v1X && mouseX < v2X && mouseX < v3X)
			return false;

		return mouseX <= v1X || mouseX <= v2X || mouseX <= v3X;
	}
}
