package mynameisjeff.skyblockclientupdater.gui.elements

import gg.essential.elementa.UIComponent
import gg.essential.elementa.components.UIContainer
import gg.essential.elementa.components.UIText
import gg.essential.elementa.constraints.ChildBasedSizeConstraint
import gg.essential.elementa.constraints.SiblingConstraint
import gg.essential.elementa.constraints.animation.Animations
import gg.essential.elementa.dsl.*
import gg.essential.universal.ChatColor
import mynameisjeff.skyblockclientupdater.SkyClientUpdater
import java.awt.Color
import java.io.File

class ModUpdateComponent(
    update: Triple<File, String, String>,
    updating: MutableSet<Triple<File, String, String>>
) : UIComponent() {
    val oldFileText = UIText(update.first.name).constrain {
        color = Color(179, 0, 0).toConstraint()
    } childOf this
    val seperatorContainer = UIContainer().constrain {
        x = SiblingConstraint(2f)
    } childOf this
    val seperatorText = UIText("${ChatColor.BOLD}\u279C").constrain {
        color = Color.GREEN.toConstraint()
    } childOf seperatorContainer
    val newFileText = UIText("${ChatColor.GREEN}${update.second}").constrain {
        x = SiblingConstraint(2f)
        color = Color(66, 245, 93).toConstraint()
    } childOf this

    init {
        seperatorContainer.constrain {
            width = seperatorText.constraints.width
            height = seperatorText.constraints.height
        }

        constrain {
            width = ChildBasedSizeConstraint()
            height = newFileText.constraints.height
        }.onMouseClick {
            if (updating.contains(update)) {
                seperatorText.animate { setColorAnimation(Animations.OUT_EXP, 1f, Color(245, 66, 66).toConstraint()) }
                updating.remove(update)
            } else {
                seperatorText.animate { setColorAnimation(Animations.OUT_EXP, 1f, Color(66, 245, 93).toConstraint()) }
                updating.add(update)
            }
        }
    }
}