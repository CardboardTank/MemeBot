package jdabot;

import net.dv8tion.jda.core.audio.AudioReceiveHandler;
import net.dv8tion.jda.core.audio.CombinedAudio;
import net.dv8tion.jda.core.audio.UserAudio;

public class AudioReceiver implements AudioReceiveHandler {
	
	private byte[] lastFrame;

	public boolean canReceiveCombined() {
		return true;
	}

	public boolean canReceiveUser() {
		return false;
	}

	public void handleCombinedAudio(CombinedAudio combinedAudio) {
		if (combinedAudio.getUsers().isEmpty()) return;
		lastFrame = combinedAudio.getAudioData(1.0);
	}

	public void handleUserAudio(UserAudio userAudio) {}
	
	public byte[] provide()
	{
		byte[] ret = lastFrame;
		lastFrame = null;
		return ret;
	}

}
