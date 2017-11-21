package jdabot;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;

import net.dv8tion.jda.core.audio.AudioSendHandler;

public class AudioTransmitter implements AudioSendHandler {
	
	private AudioPlayer player;
	private AudioReceiver receiver;
	private byte[] lastFrame;
	
	private boolean relayAudio;
	
	public AudioTransmitter(AudioPlayer player, AudioReceiver receiver)
	{
		this.player = player;
		this.receiver = receiver;
	}
	
	public boolean canProvide() {
		if (relayAudio)
		{
			lastFrame = receiver.provide();
		}
		else
		{
			AudioFrame frame = player.provide();
			lastFrame = (frame == null) ? null : frame.data;
		}
		return lastFrame != null;
	}

	public byte[] provide20MsAudio() {
		return lastFrame;
	}
	
	public void setRelay(boolean relayAudio)
	{
		this.relayAudio = relayAudio;
	}
	
	public void setReceiver(AudioReceiver receiver)
	{
		this.receiver = receiver;
	}
	
	public boolean isOpus()
	{
		return !relayAudio;
	}

}
