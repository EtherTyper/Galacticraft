package micdoodle8.mods.galacticraft.core.dimension;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import micdoodle8.mods.galacticraft.api.galaxies.CelestialBody;
import micdoodle8.mods.galacticraft.api.galaxies.GalaxyRegistry;
import micdoodle8.mods.galacticraft.api.vector.Vector3;
import micdoodle8.mods.galacticraft.core.GalacticraftCore;
import micdoodle8.mods.galacticraft.core.network.PacketSimple;
import micdoodle8.mods.galacticraft.core.network.PacketSimple.EnumSimplePacket;
import micdoodle8.mods.galacticraft.core.util.EnumColor;
import micdoodle8.mods.galacticraft.core.util.GCCoreUtil;
import micdoodle8.mods.galacticraft.core.util.PlayerUtil;
import micdoodle8.mods.galacticraft.core.wrappers.FlagData;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.management.PlayerList;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.FMLCommonHandler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SpaceRaceManager
{
    private static final Set<SpaceRace> spaceRaces = Sets.newHashSet();

    public static SpaceRace addSpaceRace(SpaceRace spaceRace)
    {
        SpaceRaceManager.spaceRaces.remove(spaceRace);
        SpaceRaceManager.spaceRaces.add(spaceRace);
        return spaceRace;
    }

    public static void removeSpaceRace(SpaceRace race)
    {
        SpaceRaceManager.spaceRaces.remove(race);
    }

    public static void tick()
    {
        for (SpaceRace race : SpaceRaceManager.spaceRaces)
        {
            boolean playerOnline = false;

            PlayerList playerList = FMLCommonHandler.instance().getMinecraftServerInstance().getPlayerList();
            for (int j = 0; j < playerList.getPlayerList().size(); j++)
            {
                EntityPlayer player = playerList.getPlayerList().get(j);

                if (race.getPlayerNames().contains(player.getGameProfile().getName()))
                {
                    CelestialBody body = GalaxyRegistry.getCelestialBodyFromDimensionID(player.worldObj.provider.getDimension());

                    if (body != null)
                    {
                        if (!race.getCelestialBodyStatusList().containsKey(body))
                        {
                            race.setCelestialBodyReached(body);
                        }
                    }

                    playerOnline = true;
                }
            }

            if (playerOnline)
            {
                race.tick();
            }
        }
    }

    public static void loadSpaceRaces(NBTTagCompound nbt)
    {
        NBTTagList tagList = nbt.getTagList("SpaceRaceList", 10);

        for (int i = 0; i < tagList.tagCount(); i++)
        {
            NBTTagCompound nbt2 = tagList.getCompoundTagAt(i);
            SpaceRace race = new SpaceRace();
            race.loadFromNBT(nbt2);
            SpaceRaceManager.spaceRaces.add(race);
        }
    }

    public static NBTTagCompound saveSpaceRaces(NBTTagCompound nbt)
    {
        NBTTagList tagList = new NBTTagList();

        for (SpaceRace race : SpaceRaceManager.spaceRaces)
        {
            NBTTagCompound nbt2 = new NBTTagCompound();
            race.saveToNBT(nbt2);
            tagList.appendTag(nbt2);
        }

        nbt.setTag("SpaceRaceList", tagList);
        return nbt;
    }

    public static SpaceRace getSpaceRaceFromPlayer(String username)
    {
        for (SpaceRace race : SpaceRaceManager.spaceRaces)
        {
            if (race.getPlayerNames().contains(username))
            {
                return race;
            }
        }

        return null;
    }

    public static SpaceRace getSpaceRaceFromID(int teamID)
    {
        for (SpaceRace race : SpaceRaceManager.spaceRaces)
        {
            if (race.getSpaceRaceID() == teamID)
            {
                return race;
            }
        }

        return null;
    }

    public static void sendSpaceRaceData(EntityPlayerMP toPlayer, SpaceRace spaceRace)
    {
        if (spaceRace != null)
        {
            List<Object> objList = new ArrayList<Object>();
            objList.add(spaceRace.getSpaceRaceID());
            objList.add(spaceRace.getTeamName());
            objList.add(spaceRace.getFlagData());
            objList.add(spaceRace.getTeamColor());
            objList.add(spaceRace.getPlayerNames().toArray(new String[spaceRace.getPlayerNames().size()]));

            if (toPlayer != null)
            {
                GalacticraftCore.packetPipeline.sendTo(new PacketSimple(EnumSimplePacket.C_UPDATE_SPACE_RACE_DATA, toPlayer.worldObj.provider.getDimension(), objList), toPlayer);
            }
            else
            {
                for (WorldServer server : toPlayer.worldObj.getMinecraftServer().worldServers)
                {
                    GalacticraftCore.packetPipeline.sendToDimension(new PacketSimple(EnumSimplePacket.C_UPDATE_SPACE_RACE_DATA, server.provider.getDimension(), objList), server.provider.getDimension());
                }
            }
        }
    }

    public static ImmutableSet<SpaceRace> getSpaceRaces()
    {
        return ImmutableSet.copyOf(new HashSet<SpaceRace>(SpaceRaceManager.spaceRaces));
    }

    public static void onPlayerRemoval(String player, SpaceRace race)
    {
        for (String member : race.getPlayerNames())
        {
            EntityPlayerMP memberObj = PlayerUtil.getPlayerForUsernameVanilla(FMLCommonHandler.instance().getMinecraftServerInstance(), member);

            if (memberObj != null)
            {
                memberObj.addChatMessage(new TextComponentString(EnumColor.DARK_AQUA + GCCoreUtil.translateWithFormat("gui.space_race.chat.remove_success", EnumColor.RED + player + EnumColor.DARK_AQUA)).setStyle(new Style().setColor(TextFormatting.DARK_AQUA)));
            }
        }

        List<String> playerList = new ArrayList<String>();
        playerList.add(player);
        SpaceRace newRace = SpaceRaceManager.addSpaceRace(new SpaceRace(playerList, SpaceRace.DEFAULT_NAME, new FlagData(48, 32), new Vector3(1, 1, 1)));
        EntityPlayerMP playerToRemove = PlayerUtil.getPlayerBaseServerFromPlayerUsername(player, true);

        if (playerToRemove != null)
        {
            SpaceRaceManager.sendSpaceRaceData(playerToRemove, newRace);
            SpaceRaceManager.sendSpaceRaceData(playerToRemove, race);
        }
    }
}
