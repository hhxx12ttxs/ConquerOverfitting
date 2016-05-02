package codechicken.wirelessredstone.core;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import codechicken.core.CommonUtils;
import codechicken.core.ServerUtils;
import codechicken.core.commands.CoreCommand.WCommandSender;

public class ParamJam extends FreqParam
{
    public static String[] examples = new String[]{
        "Eg. freq jam codechicken 100-250", 
        "Eg. freq jam codechicken all", 
        "Eg. freq jam codechicken default", 
        "Eg. freq jam 100-250", 
        "Eg. freq jam codechicken 500"};
    
    @Override
    public void printHelp(WCommandSender listener)
    {
        listener.sendChatToPlayer("Usage: freq jam [playername] [frequency range | all | default].");
        listener.sendChatToPlayer("Jam [playername] from [frequency range].");
        listener.sendChatToPlayer(examples[rand.nextInt(4)]);        
    }

    @Override
    public String getName()
    {
        return "jam";
    }

    @Override
    public void handleCommand(String playername, String[] subArray, WCommandSender listener)
    {
        jamOpenCommand(playername, CommonUtils.subArray(subArray, 1), listener, true);
    }
    
    public static void jamOpenCommand(String playername, String[] subArray, WCommandSender listener, boolean jam)
    {
        RedstoneEtherServer ether = RedstoneEther.server();
        
        if(subArray.length == 0)
        {
            listener.sendChatToPlayer("Invalid number of parameters.");
            return;
        }
        
        if((subArray.length == 1 && ServerUtils.getPlayer(playername) == null))
        {
            listener.sendChatToPlayer("No such Player.");
            return;
        }
        
        String range = subArray[subArray.length-1];
        String jamPlayer = subArray.length == 1 ? playername : subArray[0];

        int startfreq;
        int endfreq;

        if(range.equals("all"))
        {
            startfreq = 1;
            endfreq = RedstoneEther.numfreqs;
        }
        else if(range.equals("default"))
        {
            startfreq = ether.getLastSharedFrequency()+1;
            endfreq = RedstoneEther.numfreqs;
        }
        else
        {
            int[] freqrange = RedstoneEther.parseFrequencyRange(range);
            startfreq = freqrange[0];
            endfreq = freqrange[1];
        }

        if(startfreq < 1 || endfreq > RedstoneEther.numfreqs || endfreq < startfreq)
        {
            listener.sendChatToPlayer("Invalid Frequency Range.");
            return;
        }

        ether.setFrequencyRangeCommand(jamPlayer, startfreq, endfreq, jam);

        StringBuilder returnstring = (new StringBuilder());

        if(jam)
        {
            returnstring.append("jammed from");
        }
        else
        {
            returnstring.append("granted");
        }

        if(startfreq == endfreq)
        {
            if(startfreq <= ether.getLastPublicFrequency())
            {
                listener.sendChatToPlayer("You can't jam someone from public frequencies.");
                return;
            }
            returnstring.append(" frequency: ").append(startfreq).append(".");
        }
        else
        {
            int publicend = ether.getLastPublicFrequency();
            if(startfreq <= publicend && endfreq <= publicend)
            {
                listener.sendChatToPlayer("You can't jam someone from public frequencies.");
                return;
            }
            if(startfreq <= publicend)
            {
                startfreq = publicend + 1;
            }
            returnstring.append(" frequencies: ").append(startfreq).append("-").append(endfreq).append(".");
        }

        String opstring = jamPlayer+" was "+returnstring;
        String playerstring = CommonUtils.colourPrefix(0xE)+"You have been "+returnstring;

        ServerUtils.sendChatToOps(playername+": "+opstring);
        
        EntityPlayer player = ServerUtils.getPlayer(jamPlayer);
        if(player != null)
            ServerUtils.sendChatTo((EntityPlayerMP) player, playerstring);
    }
}

