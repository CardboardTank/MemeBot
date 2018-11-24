package jdabot;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.security.auth.login.LoginException;

import org.json.JSONArray;
import org.json.JSONObject;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.audio.hooks.ConnectionStatus;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Game.GameType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.managers.AudioManager;

public class MemeBot implements EventListener, Runnable {
	
	public static boolean DEBUG_MODE;
	public static String DEBUG_CHANNEL;
	public static String OWNER_ID;
	public static String[] ADMIN_IDS;
	public static String PM_CHANNEL;
	
	public static String HELP_ADMIN;
	
	private static final SimpleLog LOG = SimpleLog.getLog(MemeBot.class.getName());
	
	private JDA jda;
	private File[] kitties;
	private File salt, cuffsGif;
	private VoiceChannel currentChannel, relayChannel;
	private MessageChannel currentMsgChannel;
	private AudioManager audioManager, relayManager;
	private AudioPlayer audioPlayer;
	private TrackScheduler trackScheduler;
	private AudioPlayerManager playerManager;
	private AudioLoader audioLoader;
	private AudioTransmitter audioTransmitter, relayTransmitter;
	private AudioReceiver audioReceiver, relayReceiver;
	
	private boolean voiceConnected, nextTrackLoop;
	private int relayStatus = 0; // 0 = no relay; 1 = one-way relay; 2 = two-way conduit
	
	private Thread loopThread;
	
	private ArrayList<Guild> scrambleGuilds;
	
	private Random rand;

	// link: https://discordapp.com/api/oauth2/authorize?client_id=364954414148091905&scope=bot&permissions=0
	
	public static void main(String[] args)
		throws LoginException, RateLimitedException, InterruptedException
	{
		String jsonStr = "";
		HELP_ADMIN = "";
		try {
			for (String line : Files.readAllLines(Paths.get("config.json")))
			{
				jsonStr += line;
			}
			for (String line : Files.readAllLines(Paths.get("help_admin.txt")))
			{
				HELP_ADMIN += line + "\n";
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		JSONObject cfg = new JSONObject(jsonStr);
		DEBUG_MODE = cfg.getBoolean("debug_mode");
		DEBUG_CHANNEL = cfg.getString("debug_channel");
		OWNER_ID = cfg.getString("owner_id");
		JSONArray admins = cfg.getJSONArray("admin_ids");
		ADMIN_IDS = new String[admins.length() + 1];
		PM_CHANNEL = cfg.getString("pm_channel");
		
		ADMIN_IDS[0] = OWNER_ID;
		for (int i = 0; i < admins.length(); i++)
		{
			ADMIN_IDS[i+1] = admins.getString(i);
		}
		
		MemeBot bot = new MemeBot();
		JDA jda = new JDABuilder(AccountType.BOT).setToken(cfg.getString("bot_token")).addEventListener(bot).buildAsync();
		bot.jda = jda;
		bot.initFiles();
	}

	public void onEvent(Event e) {
		if (e instanceof ReadyEvent)
		{
			start();
			jda.addEventListener(new EventListenerImpl(this));
			
			scrambleGuilds = new ArrayList<Guild>();
			rand = new Random();
			
			loopThread = new Thread(this);
			loopThread.start();
		}
	}
	
	private void initFiles()
	{
		salt = new File(Strings.saltPath);
		kitties = new File(Strings.kittyFolder).listFiles(new FileFilter() {
			public boolean accept(File f) {
				if (f.isFile())
				{
					String ext = f.getName().substring(f.getName().lastIndexOf(".")+1);
					if (ext.equals("png") || ext.equals("jpg"))
					{
						return true;
					}
				}
				return false;
			}
		});
		cuffsGif = new File(Strings.cuffsPath);
	}
	private void start()
	{
		voiceConnected = false;
		jda.getPresence().setGame(Game.of(GameType.DEFAULT, "with your credit card"));
		playerManager = new DefaultAudioPlayerManager();
		AudioSourceManagers.registerRemoteSources(playerManager);
		
		audioPlayer = playerManager.createPlayer();
		trackScheduler = new TrackScheduler(audioPlayer);
		audioPlayer.addListener(trackScheduler);
		
		audioReceiver = new AudioReceiver();
		relayReceiver = new AudioReceiver();
		
		audioTransmitter = new AudioTransmitter(audioPlayer, relayReceiver);
		relayTransmitter = new AudioTransmitter(null, audioReceiver);
		
		audioLoader = new AudioLoader();
	}
	
	public JDA getJDA()
	{
		return jda;
	}
	
	public VoiceChannel findAdminChannel(String adminId)
	{
		User admin = jda.getUserById(adminId);
		List<Guild> guilds = admin.getMutualGuilds();
		ArrayList<VoiceChannel> channels = new ArrayList<VoiceChannel>();
		
		for (int i = 0; i < guilds.size(); i++)
		{
			channels.addAll(guilds.get(i).getVoiceChannels());
		}
		
		List<Member> members;
		for (int i = 0; i < channels.size(); i++)
		{
			members = channels.get(i).getMembers();
			for (int j = 0; j < members.size(); j++)
			{
				if (members.get(j).getUser().getId().equals(adminId))
				{
					return channels.get(i);
				}
			}
		}
		
		return null;
	}
	
	public void sendCube(Message msg)
	{
		LOG.info("Sending a cube for message \"" + msg.getContentDisplay() + "\" in channel " + msg.getChannel().getName() + " (id: " + msg.getChannel().getId() + ")");
		msg.getChannel().sendMessage("```" + Strings.cubeString(msg.getContentDisplay()) + "```").queue();
	}
	
	public void sendSalt(Message msg)
	{
		LOG.info("Sending some salt for message \"" + msg.getContentDisplay() + "\" in channel " + msg.getChannel().getName() + " (id: " + msg.getChannel().getId() + ")");
		String saltMsg = Strings.constructSaltShipment(msg.getAuthor());
		msg.getChannel().sendFile(salt, new MessageBuilder().append(saltMsg).build()).queue();
	}
	
	public void sendKitty(Message msg)
	{
		LOG.info("Sending a kitty for message \"" + msg.getContentDisplay() + "\" in channel " + msg.getChannel().getName() + " (id: " + msg.getChannel().getId() + ")");
		int index = (int) Math.floor(Math.random() * kitties.length);
		int pIndex = Strings.sadMsg.indexOf("%");
		String str = Strings.sadMsg.substring(0, pIndex) + msg.getAuthor().getAsMention() + Strings.sadMsg.substring(pIndex+1);
		
		msg.getChannel().sendFile(kitties[index], new MessageBuilder().append(str).build()).queue();
	}
	
	public void sendCuffs(Message msg)
	{
		LOG.info("Sending some cuffs for message \"" + msg.getContentDisplay() + "\" in channel " + msg.getChannel().getName() + " (id: " + msg.getChannel().getId() + ")");
		msg.getChannel().sendFile(cuffsGif, new MessageBuilder().append(msg.getAuthor()).build()).queue();
	}
	
	public void setChannel(VoiceChannel channel)
	{
		currentChannel = channel;
		audioManager = currentChannel.getGuild().getAudioManager();
		audioManager.setSendingHandler(audioTransmitter);
		LOG.info("Set channel to " + channel.getName() + " (id: " + channel.getId() + ")");
	}
	
	public boolean setRelayChannel(VoiceChannel channel)
	{
		if (audioManager == null || !audioManager.getConnectionStatus().equals(ConnectionStatus.CONNECTED)) return false;
		relayChannel = channel;
		relayManager = relayChannel.getGuild().getAudioManager();
		relayManager.setSendingHandler(relayTransmitter);
		relayManager.openAudioConnection(relayChannel);
		LOG.info("Set relay channel to " + channel.getName() + "(id: " + channel.getId() + ")");
		
		if (relayStatus == 0)
		{
			setRelayStatus(1);
			LOG.info("Relay mode automatically set to one-way.");
		}

		return true;
	}
	
	public void setRelayStatus(int status)
	{
		switch (status)
		{
		case 0:
			audioTransmitter.setRelay(false);
			relayManager.setReceivingHandler(null);
			relayTransmitter.setRelay(false);
			audioManager.setReceivingHandler(null);
			relayManager.closeAudioConnection();
			relayChannel = null;
			break;
		case 1:
			audioTransmitter.setRelay(false);
			relayManager.setReceivingHandler(null);
			relayTransmitter.setRelay(true);
			audioManager.setReceivingHandler(audioReceiver);
			break;
		case 2:
			audioTransmitter.setRelay(true);
			relayManager.setReceivingHandler(relayReceiver);
			relayTransmitter.setRelay(true);
			audioManager.setReceivingHandler(audioReceiver);
			
			if (!audioPlayer.isPaused() || !trackScheduler.isTrackSet())
			{
				trackScheduler.pause();
			}
		}
		relayStatus = status;
	}
	
	public void setMsgChannel(MessageChannel channel)
	{
		currentMsgChannel = channel;
		LOG.info("Set message channel to " + channel.getName() + " (id: " + channel.getId() + ")");
	}
	
	public VoiceChannel getVoiceChannel()
	{
		return currentChannel;
	}
	
	public VoiceChannel getRelayChannel()
	{
		return relayChannel;
	}
	
	public int getRelayMode()
	{
		return relayStatus;
	}
	
	public boolean isTrackLooping()
	{
		return nextTrackLoop;
	}
	
	public void setTrackLooping(boolean loop)
	{
		nextTrackLoop = loop;
	}
	
	public boolean sendMessage(String msg)
	{
		if (currentMsgChannel == null) return false;
		
		currentMsgChannel.sendMessage(msg).queue();
		return true;
	}
	
	public boolean connectAudio()
	{
		if (currentChannel == null || voiceConnected) return false;
		
		audioManager.openAudioConnection(currentChannel);
		voiceConnected = true;
		
		return true;
	}
	
	public boolean reconnectChannel()
	{
		if (currentChannel == null) return false;
		
		List<Member> members = currentChannel.getMembers();
		for (int i = 0; i < members.size(); i++)
		{
			if (members.get(i).getUser().getId().equals(jda.getSelfUser().getId()))
			{
				return false;
			}
		}
		
		audioManager.openAudioConnection(currentChannel);
		return true;
	}
	
	public void disconnectAudio()
	{
		if (audioManager != null)
		{
			voiceConnected = false;
			audioManager.closeAudioConnection();
		}
	}
	
	public boolean isConnected()
	{
		return voiceConnected;
	}
	
	public void loadYoutube(MessageChannel channel, String id, boolean loop)
	{
		audioLoader.responseChannel = channel;
		playerManager.loadItem(Strings.getYoutubeUrl(id), audioLoader);
		nextTrackLoop = loop;
	}
	
	public TrackScheduler getTrackScheduler()
	{
		return trackScheduler;
	}
	
	private class AudioLoader implements AudioLoadResultHandler {
		
		private MessageChannel responseChannel;
		public void loadFailed(FriendlyException e) {
			responseChannel.sendMessage("Oh no, that sucks (" + e.severity.toString() + "): ```" + e.getLocalizedMessage() + "```").queue();
		}

		public void noMatches() {
			responseChannel.sendMessage("Wow! I found some null!").queue();
		}

		public void playlistLoaded(AudioPlaylist playlist) {
			responseChannel.sendMessage("I don't like playlists.").queue();
		}

		public void trackLoaded(AudioTrack track) {
			if (nextTrackLoop)
			{
				responseChannel.sendMessage("Next track will loop.").queue();
			}
			responseChannel.sendMessage("Found \"" + track.getInfo().title + "\". Use !play to start it.").queue();
			trackScheduler.setTrack(track, nextTrackLoop);
		}
		
	}
	
	public int randomInt(int max)
	{
		return rand.nextInt(max);
	}
	
	public void scrambleGuild(Guild guild)
	{
		for (Guild g : scrambleGuilds)
		{
			if (g.getId().equals(guild.getId())) return;
		}
		
		ArrayList<Member> members = new ArrayList<Member>();
		List<VoiceChannel> channels = guild.getVoiceChannels();
		for (VoiceChannel vc : channels)
		{
			members.addAll(vc.getMembers());
		}
		
		for (Member m: members)
		{
			guild.getController().moveVoiceMember(m, channels.get(randomInt(channels.size()))).queue();
		}

	}
	
	public void exit()
	{
		loopThread.interrupt();
	}

	public void run()
	{
		while (true)
		{
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				System.out.println("Exiting...");
				break;
			}
		}
	}

}
