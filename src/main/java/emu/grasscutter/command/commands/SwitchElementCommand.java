package emu.grasscutter.command.commands;

import emu.grasscutter.GameConstants;
import emu.grasscutter.Grasscutter;
import emu.grasscutter.command.Command;
import emu.grasscutter.command.CommandHandler;
import emu.grasscutter.data.GameData;
import emu.grasscutter.data.excels.AvatarData;
import emu.grasscutter.data.excels.AvatarSkillDepotData;
import emu.grasscutter.game.avatar.Avatar;
import emu.grasscutter.game.entity.EntityAvatar;
import emu.grasscutter.game.player.Player;
import emu.grasscutter.game.props.ElementType;
import emu.grasscutter.server.packet.send.PacketAbilityChangeNotify;
import emu.grasscutter.server.packet.send.PacketAvatarFightPropNotify;
import emu.grasscutter.server.packet.send.PacketAvatarSkillDepotChangeNotify;
import emu.grasscutter.server.packet.send.PacketSceneEntityAppearNotify;

import java.util.ArrayList;
import java.util.List;

@Command(label = "switchelement", usage = "switchelement [White/Anemo/Geo/Electro]", aliases = {"se"}, permission = "player.switchElement", threading = true, description = "commands.switchElement.description")
public class SwitchElementCommand implements CommandHandler {
    @Override
    public void execute(Player sender, Player targetPlayer, List<String> args) {
        if (args.size() != 1) {
            CommandHandler.sendTranslatedMessage(sender, "commands.switchElement.usage");
            return;
        }
        if (targetPlayer == null) {
            Grasscutter.getLogger().info("Must target a player");
            return;
        }

        String element = args.get(0);

        ElementType elementType = switch (element.toLowerCase()) {
            case "white" -> ElementType.Default;
            case "anemo" -> ElementType.Wind;
            case "geo" -> ElementType.Rock;
            case "electro" -> ElementType.Electric;
            default -> null;
        };

        if (elementType == null) {
            CommandHandler.sendTranslatedMessage(sender, "commands.switchElement.invalidElement");
            return;
        }

        try {
            Avatar male = targetPlayer.getAvatars().getAvatarById(GameConstants.MAIN_CHARACTER_MALE);
            Avatar female = targetPlayer.getAvatars().getAvatarById(GameConstants.MAIN_CHARACTER_FEMALE);

            if (male != null) {
                AvatarSkillDepotData skillDepot = GameData.getAvatarSkillDepotDataMap().get(500 + elementType.getDepotValue());

                //Set skill depot
                male.setSkillDepotData(skillDepot);
                male.save();

                //Ability change packet
                targetPlayer.sendPacket(new PacketAvatarSkillDepotChangeNotify(male));

                if (male.getAsEntity() != null)
                    targetPlayer.sendPacket(new PacketAbilityChangeNotify(male.getAsEntity()));

                targetPlayer.sendPacket(new PacketAvatarFightPropNotify(male));
            }

            if (female != null) {
                AvatarSkillDepotData skillDepot = GameData.getAvatarSkillDepotDataMap().get(700 + elementType.getDepotValue());

                //Set skill depot
                female.setSkillDepotData(skillDepot);
                female.save();

                //Ability change packet
                targetPlayer.sendPacket(new PacketAvatarSkillDepotChangeNotify(female));

                if (female.getAsEntity() != null)
                    targetPlayer.sendPacket(new PacketAbilityChangeNotify(female.getAsEntity()));

                targetPlayer.sendPacket(new PacketAvatarFightPropNotify(female));
            }
            CommandHandler.sendTranslatedMessage(sender, "commands.switchElement.success", element);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
