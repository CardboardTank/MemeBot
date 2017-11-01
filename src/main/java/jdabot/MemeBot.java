package jdabot;

import javax.security.auth.login.LoginException;

import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

public class MemeBot {
	
	public static boolean DEBUG_MODE;
	public static String DEBUG_CHANNEL;

	// link: https://discordapp.com/api/oauth2/authorize?client_id=364954414148091905&scope=bot&permissions=0
	
	public static void main(String[] args)
		throws LoginException, RateLimitedException, InterruptedException
	{
		JDA jda = new JDABuilder(AccountType.BOT).setToken("MzY0OTU0NDE0MTQ4MDkxOTA1.DLXUlg.lHE98Tswg4hN2KsiO93QNWad7gg").buildBlocking();
	}

}
