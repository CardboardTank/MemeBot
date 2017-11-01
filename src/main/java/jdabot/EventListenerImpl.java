package jdabot;

import java.io.File;

import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
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
		if (MemeBot.DEBUG_MODE && !event.getChannel().getId().equals(MemeBot.DEBUG_CHANNEL))
		{
			return;
		}
		
		if (event.getAuthor().getId().equals(bot.getJDA().getSelfUser().getId()))
		{
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
		
		if (cmd[0].equals("quit"))
		{
			msg.getChannel().sendMessage("**Disconnecting...**\n\n*\"" + Strings.getDisconnectFlavorText() + "\"*").queue();
			bot.getJDA().shutdown();
		}
	}
	
	private String[] parseCommand(String in)
	{
		String cmd = in.substring(1).trim();
		String[] parts = cmd.split(" ");
		return parts;
	}
}
