package jdabot;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;

import net.dv8tion.jda.core.audio.AudioSendHandler;

public class AudioTransmitter implements AudioSendHandler {
	
	private AudioPlayer player;
	private AudioFrame lastFrame;
	
	public AudioTransmitter(AudioPlayer player)
	{
		this.player = player;
	}
	
	public boolean canProvide() {
		lastFrame = player.provide();
		return lastFrame != null;
	}

	public byte[] provide20MsAudio() {
		return lastFrame.data;
	}
	
	public boolean isOpus()
	{
		return true;
	}

}
