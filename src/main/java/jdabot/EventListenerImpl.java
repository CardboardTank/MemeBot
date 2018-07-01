package jdabot;

import java.util.Arrays;
import java.util.List;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.PrivateChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.entities.VoiceChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class EventListenerImpl extends ListenerAdapter {
	
	private MemeBot bot;
	
	public EventListenerImpl(MemeBot bot)
	{
		this.bot = bot;
	}
	
	public void onMessageReceived(MessageReceivedEvent event)
	{
		if (event.getMessage().getContentDisplay().isEmpty())
		{
			return;
		}
		if (MemeBot.DEBUG_MODE && ((!event.getChannel().getId().equals(MemeBot.DEBUG_CHANNEL)) &&
				!(isIdAdmin(event.getAuthor().getId()) && event.getChannelType().equals(ChannelType.PRIVATE))))
		{
			return;
		}
		
		if (event.getAuthor().isBot())
		{
			return;
		}
		
		if (event.getChannelType().equals(ChannelType.PRIVATE) && !(isIdAdmin(event.getAuthor().getId())))
		{
			String fwd = event.getAuthor().getName() + " (" + event.getAuthor().getId() + ") sends: " + event.getMessage().getContentDisplay();
			bot.getJDA().getTextChannelById(MemeBot.PM_CHANNEL).sendMessage(fwd).queue();
			return;
		}
		
		Message msg = event.getMessage();
		String msgStr = msg.getContentDisplay();
		
		if (msgStr.charAt(0) == '!')
		{
			executeCommand(msg);
		}
		else if (Strings.testCubeStr(msgStr))
		{
			bot.sendCube(msg);
		}
		else if (Strings.isStringSalty(msgStr))
		{
			bot.sendSalt(msg);
		}
		else if (Strings.isStringSad(msgStr))
		{
			bot.sendKitty(msg);
		}
		else if (msgStr.indexOf(Strings.cuffTrigger) != -1)
		{
			bot.sendCuffs(msg);
		}
	}
	
	private boolean isIdAdmin(String id) {
		for (int i = 0; i < MemeBot.ADMIN_IDS.length; i++) {
			if (MemeBot.ADMIN_IDS[i].equals(id)) {
				return true;
			}
		}
		return false;
	}
	
	private void executeCommand(Message msg)
	{
		String[] cmd = parseCommand(msg.getContentDisplay());
		if (cmd.length == 0 || cmd[0].trim().isEmpty())
		{
			reply(msg, "What the hell are you trying to do!?");
			return;
		}
		
		if (cmd[0].equals("ping"))
		{
			reply(msg, "Pong!");
		}
		else if (msg.getChannelType().equals(ChannelType.PRIVATE) && isIdAdmin(MemeBot.ADMIN_ID))
		{
			executeAdminCommand(msg, cmd);
		}
	}
	
	private void executeAdminCommand(Message msg, String[] cmd)
	{
		if (cmd[0].equals("quit"))
		{
			msg.getChannel().sendMessage("**Disconnecting...**\n\n*\"" + Strings.getDisconnectFlavorText() + "\"*").queue((m) -> bot.getJDA().shutdown());
		}
		else if (cmd[0].equals("help"))
		{
			showHelpMessage(msg, cmd, true);
		}
		else if (cmd[0].equals("showperms"))
		{
			showGuildPermissions(msg, cmd);
		}
		else if (cmd[0].equals("setmsgchannel"))
		{
			setTextChannel(msg, cmd);
		}
		else if (cmd[0].equals("setuserchannel"))
		{
			setPrivateChannel(msg, cmd);
		}
		else if (cmd[0].equals("findchannel"))
		{
			readVoiceChannel(msg, cmd);
		}
		else if (cmd[0].equals("setvoicechannel"))
		{
			setVoiceChannel(msg, cmd);
		}
		else if (cmd[0].equals("connect"))
		{
			connectVoice(msg, cmd);
		}
		else if (cmd[0].equals("disconnect"))
		{
			disconnectVoice(msg, cmd);
		}
		else if (cmd[0].equals("reconnect"))
		{
			reconnectVoice(msg, cmd);
		}
		else if (cmd[0].equals("youtube"))
		{
			loadYoutube(msg, cmd);
		}
		else if (cmd[0].equals("play") || cmd[0].equals("pause") || cmd[0].equals("stop"))
		{
			playAudio(msg, cmd);
		}
		else if (cmd[0].equals("volume"))
		{
			setVolume(msg, cmd);
		}
		else if (cmd[0].equals("say"))
		{
			sayMessage(msg, cmd);
		}
		else if (cmd[0].equals("relay"))
		{
			setRelayChannel(msg, cmd);
		}
		else if (cmd[0].equals("relaymode"))
		{
			setRelayMode(msg, cmd);
		}
	}
	
	private void showHelpMessage(Message msg, String[] cmd, boolean admin)
	{
		String help = (admin) ? MemeBot.HELP_ADMIN : "unsupported";
		
		reply(msg, help);
	}
	
	private void showGuildPermissions(Message msg, String[] cmd)
	{
		if (cmd.length < 2)
		{
			reply(msg, "Please specify a guild like so: ```!setmsgchannel <guild_id>```");
		}
		else
		{
			Guild guild = guildFromId(msg, cmd[1]);
			
			if (guild != null)
			{
				List<Permission> perms = guild.getSelfMember().getPermissions();
				String out = "Permissions for server *" + guild.getName() + "*:```";
				for (Permission p : perms)
				{
					out += p.getName() + "\n";
				}
				
				out += "```";
				
				reply(msg, out);
			}
		}
		
	}
	
	private VoiceChannel voiceChannelFromId(Message msg, String channelId)
	{
		VoiceChannel channel = null;
		try {
			channel = bot.getJDA().getVoiceChannelById(channelId);
			if (channel == null)
			{
				reply(msg, "I traveled the world and I found everything except that channel.");
			}
		} catch (NumberFormatException e) {
			reply(msg, "That's not a channel, sunshine.");
		}
		
		return channel;
	}
	
	private MessageChannel messageChannelFromId(Message msg, String channelId)
	{
		MessageChannel channel = null;
		try {
			channel = bot.getJDA().getTextChannelById(channelId);
			if (channel == null)
			{
				reply(msg, "I traveled the world and I found everything except that channel.");
			}
		} catch (NumberFormatException e) {
			reply(msg, "That's not a channel, sunshine.");
		}
		
		return channel;
	}
	
	private Guild guildFromId(Message msg, String guildId)
	{
		Guild guild = null;
		try {
			guild = bot.getJDA().getGuildById(guildId);
			if (guild == null)
			{
				reply(msg, "I traveled the world and I found everything except that guild.");
			}
		} catch (NumberFormatException e) {
			reply(msg, "That's not a guild, sunshine.");
		}
		
		return guild;
	}
	
	private PrivateChannel privateChannelFromUserId(Message msg, String userId)
	{
		User user = null;
		try {
			user = bot.getJDA().getUserById(userId);
			if (user == null)
			{
				reply(msg, "That user is undocumented.");
			}
			else
			{
				return user.openPrivateChannel().complete();
			}
		} catch (NumberFormatException e) {
			reply(msg, "That's not a channel, sunshine.");
		}
		
		return null;
	}
	
	private void audioCommand(Message msg, String cmd)
	{
		if (cmd.equals("play"))
		{
			if (bot.getRelayMode() == 2)
			{
				bot.setRelayStatus(1);
				reply(msg, "Switching relay mode to one-way.");
			}
			reply(msg, "Starting/resuming...");
			bot.getTrackScheduler().resume();
		}
		else if (cmd.equals("pause"))
		{
			reply(msg, "Pausing audio...");
			bot.getTrackScheduler().pause();
		}
		else if (cmd.equals("stop"))
		{
			reply(msg, "Stopping audio...");
			bot.getTrackScheduler().stop();
		}
	}
	
	private void setTextChannel(Message msg, String[] cmd)
	{
		if (cmd.length < 2)
		{
			reply(msg, "Please specify a channel like so: ```!setmsgchannel <channel_id>```");
		}
		else
		{
			MessageChannel channel = messageChannelFromId(msg, cmd[1]);
			
			if (channel != null)
			{
				bot.setMsgChannel(channel);
				reply(msg, "Set channel to \"" + channel.getName() +"\" (type: " + channel.getType().toString() + ")");
			}
		}
	}
	
	private void setPrivateChannel(Message msg, String[] cmd)
	{
		if (cmd.length < 2)
		{
			reply(msg, "Please specify a user like so: ```!setuserchannel <user_id>```");
		}
		else
		{
			PrivateChannel channel = privateChannelFromUserId(msg, cmd[1]);
			
			if (channel != null)
			{
				bot.setMsgChannel(channel);
				reply(msg, "Set channel to private channel with user " + channel.getUser().getName() + " (channel id: " + channel.getId() + ")");
			}
		}
	}
	
	private void readVoiceChannel(Message msg, String[] cmd)
	{
		VoiceChannel channel = bot.findAdminChannel();
		if (channel == null)
		{
			reply(msg, "I can't find you anywhere!");
		}
		else if (bot.getRelayChannel() == null || !channel.getGuild().getId().equals(bot.getRelayChannel().getGuild().getId()))
		{
			bot.setChannel(channel);
			reply(msg, "Set channel to \"" + channel.getName() +"\" in guild \"" + channel.getGuild().getName() + "\"");
		}
		else
		{
			reply(msg, "I can't switch to your channel until you turn off that relay.");
		}
	}
	
	private void setVoiceChannel(Message msg, String[] cmd)
	{
		if (cmd.length < 2)
		{
			reply(msg, "Please specify a channel like so: ```!setchannel <channel_id>```");
		}
		else
		{
			VoiceChannel channel = voiceChannelFromId(msg, cmd[1]);
			
			if (channel != null)
			{
				if (bot.getRelayChannel() == null || !channel.getGuild().getId().equals(bot.getRelayChannel().getGuild().getId()))
				{
					bot.setChannel(channel);
					reply(msg, "Set channel to \"" + channel.getName() +"\" in guild \"" + channel.getGuild().getName() + "\"");
				}
				else
				{
					reply(msg, "I can't switch to that channel until you turn off that relay.");
				}
			}
		}
	}
	
	private void connectVoice(Message msg, String[] cmd)
	{
		if (bot.isConnected())
		{
			reply(msg, "I'm all ready and waiting for you.");
		}
		else
		{
			if (bot.connectAudio())
			{
				reply(msg, "Connecting to channel...");
			}
			else
			{
				reply(msg, "You still haven't told me which channel to go to.\n*Hint: use !readchannel or !setchannel*");
			}
		}
	}
	
	private void disconnectVoice(Message msg, String[] cmd)
	{
		if (bot.isConnected())
		{
			reply(msg, "Disconnecting from channel...");
		}
		else
		{
			reply(msg, "It's okay, I'm not connected.");
		}
		bot.disconnectAudio();
	}
	
	private void reconnectVoice(Message msg, String[] cmd)
	{
		if (bot.reconnectChannel())
		{
			reply(msg, "Attempting to move back into channel, please hold...");
		}
		else
		{
			reply(msg, "Everything is fine.");
		}
	}
	
	private void loadYoutube(Message msg, String[] cmd)
	{
		if (cmd.length < 2)
		{
			reply(msg, "Copy the video ID from the URL (after v=) like so: ```!youtube <video_ID>```");
		}
		else if (!bot.isConnected())
		{
			reply(msg, "I need to connect to a channel first.");
		}
		else
		{
			bot.loadYoutube(msg.getChannel(), cmd[1]);
		}
	}
	
	private void playAudio(Message msg, String[] cmd)
	{
		if (!bot.isConnected())
		{
			reply(msg, "I need to connect to a channel first.");
		}
		else if (!bot.getTrackScheduler().isTrackSet())
		{
			reply(msg, "I don't have any audio loaded at the moment.");
		}
		else
		{
			audioCommand(msg, cmd[0]);
		}
	}
	
	private void setVolume(Message msg, String[] cmd)
	{
		if (cmd.length < 2)
		{
			reply(msg, "Please specify a numeric volume like so (default is 100): ```!volume <value>```");
		}
		else
		{
			try {
				int volume = Math.min(150, Math.max(Integer.parseInt(cmd[1]), 0));
				bot.getTrackScheduler().setVolume(volume);
				reply(msg, "Set volume to " + volume + "%");
			} catch (NumberFormatException e) {
				reply(msg, "What part of 'number' do you not understand?");
			}
		}
	}
	
	private void sayMessage(Message msg, String cmd[])
	{
		if (cmd.length < 2)
		{
			reply(msg, "It's okay, I already said nothing.");
		}
		else if (bot.sendMessage(String.join(" ", Arrays.copyOfRange(cmd, 1, cmd.length))))
		{
			reply(msg, "Message queued.");
		}
		else
		{
			reply(msg, "Please specify a channel like so: ```!setmsgchannel <channel_id>```");
		}
	}
	
	private void setRelayChannel(Message msg, String cmd[])
	{
		if (cmd.length < 2)
		{
			reply(msg, "I need somewhere to relay to.");
		}
		else
		{
			VoiceChannel channel = voiceChannelFromId(msg, cmd[1]);
			
			if (channel != null)
			{
				VoiceChannel from = bot.getVoiceChannel();
				if (!(from != null && from.getGuild().getId().equals(channel.getGuild().getId())))
				{
					bot.setRelayChannel(channel);
				}
				else
				{
					reply(msg, "Relay channel can't be in the same guild.");
				}
			}
		}
	}
	
	private void setRelayMode(Message msg, String cmd[])
	{
		if (cmd.length < 2)
		{
			switch (bot.getRelayMode())
			{
			case 0:
				reply(msg, "Relay is currently disabled.");
				break;
			case 1:
				reply(msg, "One-way relay currently enabled.");
				break;
			case 2:
				reply(msg, "Two-way conduit currently enabled.");
			}
		}
		else
		{
			try {
				int status = Integer.parseInt(cmd[1]);
				if (status != 0 && status != 1 && status != 2)
				{
					reply(msg, "Relay mode must be either 0, 1, or 2");
				}
				else
				{
					bot.setRelayStatus(status);
					String str = "oof";
					switch (status)
					{
					case 0:
						str = "Relay disabled.";
						break;
					case 1:
						str = "Relay set to one-way.";
						break;
					case 2:
						str = "Relay set to two-way.";
					}
					
					reply(msg, str);
				}
			} catch (NumberFormatException e) {
				reply(msg, "Relay mode must be a number: ```(0, 1, or 2)```");
			}
		}
	}
	
	private void reply(Message msg, String txt)
	{
		msg.getChannel().sendMessage(txt).queue();
	}
	
	private String[] parseCommand(String in)
	{
		String cmd = in.substring(1).trim();
		String[] parts = cmd.split(" ");
		return parts;
	}
}
