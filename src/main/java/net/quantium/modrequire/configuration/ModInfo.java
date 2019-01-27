package net.quantium.modrequire.configuration;

import net.minecraftforge.fml.common.versioning.ArtifactVersion;
import net.minecraftforge.fml.common.versioning.DefaultArtifactVersion;

public class ModInfo {
	private final String modId;
	private final ArtifactVersion version;
	
	public ModInfo(String modId, ArtifactVersion version) {
		this.modId = modId;
		this.version = version;
	}
	
	public ModInfo(String modId, String version) {
		this(modId, new DefaultArtifactVersion(version));
	}

	public String getModId() {
		return this.modId;
	}

	public ArtifactVersion getVersion() {
		return this.version;
	}
	
	@Override
	public String toString() {
		return this.modId + " " + this.version.getVersionString();
	}
}
