package jdabot;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public class TrackScheduler extends AudioEventAdapter {
	
	private AudioPlayer player;
	
	public TrackScheduler(AudioPlayer player)
	{
		this.player = player;
	}
	
	public void setTrack(AudioTrack track)
	{
		if (player.getPlayingTrack() != null)
		{
			stop();
		}
		pause();
		player.playTrack(track);
	}
	
	public boolean isTrackSet()
	{
		return player.getPlayingTrack() != null;
	}
	
	public void stop()
	{
		player.stopTrack();
	}
	
	public void pause()
	{
		player.setPaused(true);
	}
	
	public void resume()
	{
		player.setPaused(false);
	}
	
	public void setVolume(int volume)
	{
		player.setVolume(volume);
	}
	
}
