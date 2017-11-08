package jdabot;

import java.util.Arrays;

import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
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
		if (MemeBot.DEBUG_MODE && ((!event.getChannel().getId().equals(MemeBot.DEBUG_CHANNEL)) &&
				!(event.getAuthor().getId().equals(MemeBot.ADMIN_ID) && event.getChannelType().equals(ChannelType.PRIVATE))))
		{
			return;
		}
		
		if (event.getAuthor().isBot())
		{
			return;
		}
		
		if (event.getChannelType().equals(ChannelType.PRIVATE) && !(event.getAuthor().getId().equals(MemeBot.ADMIN_ID)))
		{
			String fwd = event.getAuthor().getName() + " (" + event.getAuthor().getId() + ") sends: " + event.getMessage().getContent();
			bot.getJDA().getTextChannelById(MemeBot.PM_CHANNEL).sendMessage(fwd).queue();
			return;
		}
		
		Message msg = event.getMessage();
		String msgStr = msg.getContent();
		
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
	
	private void executeCommand(Message msg)
	{
		String[] cmd = parseCommand(msg.getContent());
		if (cmd.length == 0 || cmd[0].trim().isEmpty())
		{
			reply(msg, "What the hell are you trying to do!?");
			return;
		}
		
		if (cmd[0].equals("quit"))
		{
			msg.getChannel().sendMessage("**Disconnecting...**\n\n*\"" + Strings.getDisconnectFlavorText() + "\"*").queue((m) -> bot.getJDA().shutdown());
		}
		else if (msg.getChannelType().equals(ChannelType.PRIVATE) && msg.getAuthor().getId().equals(MemeBot.ADMIN_ID))
		{
			executeAdminCommand(msg, cmd);
		}
	}
	
	private void executeAdminCommand(Message msg, String[] cmd)
	{
		if (cmd[0].equals("setmsgchannel"))
		{
			if (cmd.length < 2)
			{
				reply(msg, "Please specify a channel like so: ```!setmsgchannel <channel_id>```");
			}
			else
			{
				MessageChannel channel = null;
				try {
					channel = bot.getJDA().getTextChannelById(cmd[1]);
					if (channel == null)
					{
						reply(msg, "I traveled the world and I found everything except that channel.");
					}
					else
					{
						bot.setMsgChannel(channel);
						reply(msg, "Set channel to \"" + channel.getName() +"\" (type: " + channel.getType().toString() + ")");
					}
				} catch (NumberFormatException e) {
					reply(msg, "That's not a channel, sunshine.");
				}
			}
		}
		else if (cmd[0].equals("setuserchannel"))
		{
			if (cmd.length < 2)
			{
				reply(msg, "Please specify a user like so: ```!setuserchannel <user_id>```");
			}
			else
			{
				User user = null;
				try {
					user = bot.getJDA().getUserById(cmd[1]);
					if (user == null)
					{
						reply(msg, "That user is undocumented.");
					}
					else
					{
						final String userName = user.getName();
						user.openPrivateChannel().queue(ch -> {
							bot.setMsgChannel(ch);
							reply(msg, "Set channel to private channel with user " + userName + " (channel id: " + ch.getId() + ")");
						});
						
					}
				} catch (NumberFormatException e) {
					reply(msg, "That's not a channel, sunshine.");
				}
			}
		}
		else if (cmd[0].equals("readchannel"))
		{
			VoiceChannel channel = bot.findAdminChannel();
			if (channel == null)
			{
				reply(msg, "I can't find you anywhere!");
			}
			else
			{
				bot.setChannel(channel);
				reply(msg, "Set channel to \"" + channel.getName() +"\" in guild \"" + channel.getGuild().getName() + "\"");
			}
		}
		else if (cmd[0].equals("setchannel"))
		{
			if (cmd.length < 2)
			{
				reply(msg, "Please specify a channel like so: ```!setchannel <channel_id>```");
			}
			else
			{
				VoiceChannel channel = null;
				try {
					channel = bot.getJDA().getVoiceChannelById(cmd[1]);
					if (channel == null)
					{
						reply(msg, "I traveled the world and I found everything except that channel.");
					}
					else
					{
						bot.setChannel(channel);
						reply(msg, "Set channel to \"" + channel.getName() +"\" in guild \"" + channel.getGuild().getName() + "\"");
					}
				} catch (NumberFormatException e) {
					reply(msg, "That's not a channel, sunshine.");
				}
			}
		}
		else if (cmd[0].equals("connect"))
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
		else if (cmd[0].equals("disconnect"))
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
		else if (cmd[0].equals("reconnect"))
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
		else if (cmd[0].equals("youtube"))
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
		else if (cmd[0].equals("play") || cmd[0].equals("pause") || cmd[0].equals("stop"))
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
		else if (cmd[0].equals("volume"))
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
		else if (cmd[0].equals("say"))
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
	}
	
	private void audioCommand(Message msg, String cmd)
	{
		if (cmd.equals("play"))
		{
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
