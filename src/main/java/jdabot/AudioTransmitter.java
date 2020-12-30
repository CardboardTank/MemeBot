package jdabot;

import java.nio.ByteBuffer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;

import net.dv8tion.jda.api.audio.AudioSendHandler;

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
			lastFrame = (frame == null) ? null : frame.getData();
		}
		return lastFrame != null;
	}

	public ByteBuffer provide20MsAudio() {
		return ByteBuffer.wrap(lastFrame);
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
