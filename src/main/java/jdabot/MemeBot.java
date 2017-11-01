package jdabot;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.security.auth.login.LoginException;

import org.json.JSONObject;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import net.dv8tion.jda.core.hooks.EventListener;
import net.dv8tion.jda.core.utils.SimpleLog;

public class MemeBot implements EventListener {
	
	public static boolean DEBUG_MODE;
	public static String DEBUG_CHANNEL;
	
	private JDA jda;
	private File[] kitties;
	private File salt, cuffsGif;
	private static final SimpleLog LOG = SimpleLog.getLog(MemeBot.class);

	// link: https://discordapp.com/api/oauth2/authorize?client_id=364954414148091905&scope=bot&permissions=0
	
	public static void main(String[] args)
		throws LoginException, RateLimitedException, InterruptedException
	{
		String jsonStr = "";
		try {
			for (String line : Files.readAllLines(Paths.get("config.json")))
			{
				jsonStr += line;
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		JSONObject cfg = new JSONObject(jsonStr);
		DEBUG_MODE = cfg.getBoolean("debug_mode");
		DEBUG_CHANNEL = cfg.getString("debug_channel");
		
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
		jda.getPresence().setGame(Game.of("with your credit card"));
	}
	
	public JDA getJDA()
	{
		return jda;
	}
	
	public void sendCube(Message msg)
	{
		LOG.info("Sending a cube for message \"" + msg.getContent() + "\" in channel " + msg.getChannel().getName() + " (id: " + msg.getChannel().getId() + ")");
		msg.getChannel().sendMessage("```" + Strings.cubeString(msg.getContent()) + "```").queue();
	}
	
	public void sendSalt(Message msg)
	{
		LOG.info("Sending some salt for message \"" + msg.getContent() + "\" in channel " + msg.getChannel().getName() + " (id: " + msg.getChannel().getId() + ")");
		String saltMsg = Strings.constructSaltShipment(msg.getAuthor());
		msg.getChannel().sendFile(salt, new MessageBuilder().append(saltMsg).build()).queue();
	}
	
	public void sendKitty(Message msg)
	{
		LOG.info("Sending a kitty for message \"" + msg.getContent() + "\" in channel " + msg.getChannel().getName() + " (id: " + msg.getChannel().getId() + ")");
		int index = (int) Math.floor(Math.random() * kitties.length);
		int pIndex = Strings.sadMsg.indexOf("%");
		String str = Strings.sadMsg.substring(0, pIndex) + msg.getAuthor().getAsMention() + Strings.sadMsg.substring(pIndex+1);
		
		msg.getChannel().sendFile(kitties[index], new MessageBuilder().append(str).build()).queue();
	}
	
	public void sendCuffs(Message msg)
	{
		LOG.info("Sending some cuffs for message \"" + msg.getContent() + "\" in channel " + msg.getChannel().getName() + " (id: " + msg.getChannel().getId() + ")");
		msg.getChannel().sendFile(cuffsGif, new MessageBuilder().append(msg.getAuthor()).build()).queue();
	}

}
