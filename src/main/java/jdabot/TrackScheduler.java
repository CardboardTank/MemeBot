package jdabot;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;

public class TrackScheduler extends AudioEventAdapter {
	
	private AudioPlayer player;
	private AudioTrack loopTrack;
	
	public TrackScheduler(AudioPlayer player)
	{
		this.player = player;
	}
	
	public void setTrack(AudioTrack track, boolean loop)
	{
		if (player.getPlayingTrack() != null)
		{
			stop();
		}
		pause();
		
		if (loop)
		{
			loopTrack = track.makeClone();
		}
		player.playTrack(track);
	}
	
	public boolean isTrackSet()
	{
		return player.getPlayingTrack() != null;
	}
	
	public void stop()
	{
		player.stopTrack();
		loopTrack = null;
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
	
	public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
		if (endReason.mayStartNext)
		{
			if (loopTrack != null)
			{
				setTrack(loopTrack, true);
			}
		}
	}
	
}
