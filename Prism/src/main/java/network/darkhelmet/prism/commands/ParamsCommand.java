package network.darkhelmet.prism.commands;

import network.darkhelmet.prism.Il8nHelper;
import network.darkhelmet.prism.Prism;
import network.darkhelmet.prism.commandlibs.CallInfo;
import network.darkhelmet.prism.commandlibs.SubHandler;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.regex.Pattern;

public class ParamsCommand implements SubHandler {

    @Override
    public void handle(CallInfo call) {
        help(call.getSender());
    }

    @Override
    public List<String> handleComplete(CallInfo call) {
        return null;
    }

    @Override
    public String[] getHelp() {
        return new String[]{Il8nHelper.getRawMessage("help-params")};
    }

    @Override
    public String getRef() {
        return "/parameters.html";
    }


    private void help(CommandSender s) {
        TextComponent.Builder builder = Component.text()
                .append(Prism.messenger.playerHeaderMsg(Il8nHelper.getMessage("params-header")
                        .color(NamedTextColor.GOLD)).append(Component.newline()))
                .append(Prism.messenger.playerMsg(colourParamHelp(
                        Il8nHelper.getMessage("params-radius-help")))).append(Component.newline())
                .append(Prism.messenger.playerMsg(
                        colourParamHelp(Il8nHelper.getMessage("params-radius-help2")))).append(Component.newline())
                .append(Prism.messenger.playerMsg(
                        colourParamHelp(Il8nHelper.getMessage("params-radius-help3")))).append(Component.newline())
                .append(Prism.messenger.playerMsg(
                        colourParamHelp(Il8nHelper.getMessage("params-radius-help4")))).append(Component.newline())
                .append(Component.text("---").color(NamedTextColor.GRAY)).append(Component.newline())
                .append(Prism.messenger.playerMsg(Component.text("a:[行为]: ").color(NamedTextColor.LIGHT_PURPLE)))
                .append(Il8nHelper.getMessage("params-help-action")).append(Component.newline())
                .append(Prism.messenger.playerMsg(Component.text("b:[方块] ").color(NamedTextColor.LIGHT_PURPLE)))
                .append(Il8nHelper.getMessage("params-help-block")).append(Component.newline())
                .append(Prism.messenger.playerMsg(Component.text("before:[时长] ").color(NamedTextColor.LIGHT_PURPLE)))
                .append(Il8nHelper.getMessage("params-help-before")).append(Component.newline())
                .append(Prism.messenger.playerMsg(Component.text("e:[实体] ").color(NamedTextColor.LIGHT_PURPLE)))
                .append(Il8nHelper.getMessage("params-help-entity")).append(Component.newline())
                .append(Prism.messenger.playerMsg(Component.text("id:[#] ").color(NamedTextColor.LIGHT_PURPLE)))
                .append(Il8nHelper.getMessage("params-help-id")).append(Component.newline())
                .append(Prism.messenger.playerMsg(Component.text("k:[文本] ").color(NamedTextColor.LIGHT_PURPLE)))
                .append(Il8nHelper.getMessage("params-help-keyword")).append(Component.newline())
                .append(Prism.messenger.playerMsg(Component.text("p:[玩家] ").color(NamedTextColor.LIGHT_PURPLE)))
                .append(Il8nHelper.getMessage("params-help-player")).append(Component.newline())
                .append(Prism.messenger.playerMsg(Component.text("since:[时长] ").color(NamedTextColor.LIGHT_PURPLE)))
                .append(Il8nHelper.getMessage("params-help-since")).append(Component.newline())
                .append(Prism.messenger.playerMsg(Component.text("t:[时长] ").color(NamedTextColor.LIGHT_PURPLE)))
                .append(Il8nHelper.getMessage("params-help-time")).append(Component.newline())
                .append(Prism.messenger.playerMsg(Component.text("w:[世界] ").color(NamedTextColor.LIGHT_PURPLE)))
                .append(Il8nHelper.getMessage("params-help-world")).append(Component.newline())
                .append(Il8nHelper.getMessage("params-help-prefix")).append(Component.newline())
                .append(Il8nHelper.getMessage("params-help-partial")).append(Component.newline())
                .append(Il8nHelper.getMessage("params-help-actionlist")).append(Component.newline());
        Prism.messenger.sendMessage(s,builder.build());
    }

    private static Component colourParamHelp(TextComponent message) {
        Pattern pattern = Pattern.compile("([abtrkpew]|id|since|before){1}:([\\[,<,a-z,0-9,>,|,:,\\],#]*)");
        return message.replaceText(pattern, builder -> builder.color(NamedTextColor.LIGHT_PURPLE));
    }
}