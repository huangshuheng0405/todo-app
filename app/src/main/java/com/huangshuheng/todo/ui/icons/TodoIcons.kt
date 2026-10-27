package com.huangshuheng.todo.ui.icons

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathBuilder
import androidx.compose.ui.graphics.vector.PathData
import androidx.compose.ui.unit.dp

private fun createIcon(
    name: String,
    block: ImageVector.Builder.() -> Unit
): ImageVector = ImageVector.Builder(
    name = name,
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = 24f,
    viewportHeight = 24f
).apply(block).build()

private fun ImageVector.Builder.stroke(
    width: Float = 1.7f,
    pathBuilder: PathBuilder.() -> Unit
) {
    addPath(
        pathData = PathData(pathBuilder),
        fill = null,
        stroke = SolidColor(Color.Black),
        strokeLineWidth = width,
        strokeLineCap = StrokeCap.Round,
        strokeLineJoin = StrokeJoin.Round
    )
}

private fun ImageVector.Builder.solid(
    pathBuilder: PathBuilder.() -> Unit
) {
    addPath(pathData = PathData(pathBuilder), fill = SolidColor(Color.Black))
}

private fun PathBuilder.circle(cx: Float, cy: Float, r: Float) {
    moveTo(cx - r, cy)
    arcToRelative(r, r, 0f, true, true, r * 2, 0f)
    arcToRelative(r, r, 0f, true, true, -r * 2, 0f)
    close()
}

/**
 * 自绘图标集：统一 24x24 视窗、1.7 描边、圆头圆角。
 * 比 material-icons-extended 更贴近 Todoist 的细线风格，也不额外增加依赖体积。
 */
object TodoIcons {

    /** 汉堡菜单 */
    val Menu: ImageVector = createIcon("Menu") {
        stroke {
            moveTo(3.5f, 6.5f); lineTo(20.5f, 6.5f)
            moveTo(3.5f, 12f); lineTo(20.5f, 12f)
            moveTo(3.5f, 17.5f); lineTo(14f, 17.5f)
        }
    }

    /** 加号 */
    val Add: ImageVector = createIcon("Add") {
        stroke(width = 1.9f) {
            moveTo(12f, 5f); lineTo(12f, 19f)
            moveTo(5f, 12f); lineTo(19f, 12f)
        }
    }

    /** 对勾 */
    val Check: ImageVector = createIcon("Check") {
        stroke(width = 2.2f) {
            moveTo(5f, 12.5f); lineTo(9.6f, 17.2f); lineTo(19f, 6.8f)
        }
    }

    /** 关闭 */
    val Close: ImageVector = createIcon("Close") {
        stroke(width = 1.9f) {
            moveTo(6.2f, 6.2f); lineTo(17.8f, 17.8f)
            moveTo(17.8f, 6.2f); lineTo(6.2f, 17.8f)
        }
    }

    /** 更多（三个点） */
    val More: ImageVector = createIcon("More") {
        solid {
            circle(5.2f, 12f, 1.6f)
            circle(12f, 12f, 1.6f)
            circle(18.8f, 12f, 1.6f)
        }
    }

    /** 收件箱 */
    val Inbox: ImageVector = createIcon("Inbox") {
        stroke {
            moveTo(3.6f, 13f); lineTo(7.6f, 13f); lineTo(9.1f, 15.6f)
            lineTo(14.9f, 15.6f); lineTo(16.4f, 13f); lineTo(20.4f, 13f)
            lineTo(20.4f, 19.2f); lineTo(3.6f, 19.2f); close()
            moveTo(3.6f, 13f); lineTo(5.4f, 4.8f); lineTo(18.6f, 4.8f); lineTo(20.4f, 13f)
        }
    }

    /** 今天（日历） */
    val Today: ImageVector = createIcon("Today") {
        stroke {
            moveTo(4.2f, 6.2f); lineTo(19.8f, 6.2f); lineTo(19.8f, 20f); lineTo(4.2f, 20f); close()
            moveTo(8f, 3.4f); lineTo(8f, 7.4f)
            moveTo(16f, 3.4f); lineTo(16f, 7.4f)
            moveTo(4.2f, 10.6f); lineTo(19.8f, 10.6f)
        }
    }

    /** 即将到来（日历 + 点） */
    val Upcoming: ImageVector = createIcon("Upcoming") {
        stroke {
            moveTo(4.2f, 6.2f); lineTo(19.8f, 6.2f); lineTo(19.8f, 20f); lineTo(4.2f, 20f); close()
            moveTo(8f, 3.4f); lineTo(8f, 7.4f)
            moveTo(16f, 3.4f); lineTo(16f, 7.4f)
            moveTo(4.2f, 10.6f); lineTo(19.8f, 10.6f)
        }
        solid {
            circle(8.4f, 14.6f, 1.1f)
            circle(12f, 14.6f, 1.1f)
            circle(15.6f, 14.6f, 1.1f)
            circle(8.4f, 17.6f, 1.1f)
            circle(12f, 17.6f, 1.1f)
        }
    }

    /** 搜索 */
    val Search: ImageVector = createIcon("Search") {
        stroke {
            moveTo(4.6f, 10.8f)
            arcToRelative(6.2f, 6.2f, 0f, true, true, 12.4f, 0f)
            arcToRelative(6.2f, 6.2f, 0f, true, true, -12.4f, 0f)
            close()
            moveTo(15.4f, 15.4f); lineTo(20.2f, 20.2f)
        }
    }

    /** 首页（清单） */
    val List: ImageVector = createIcon("List") {
        stroke {
            moveTo(8.6f, 6.4f); lineTo(20.2f, 6.4f)
            moveTo(8.6f, 12f); lineTo(20.2f, 12f)
            moveTo(8.6f, 17.6f); lineTo(20.2f, 17.6f)
        }
        solid {
            circle(4.6f, 6.4f, 1.5f)
            circle(4.6f, 12f, 1.5f)
            circle(4.6f, 17.6f, 1.5f)
        }
    }

    /** 浏览（宫格） */
    val Grid: ImageVector = createIcon("Grid") {
        stroke {
            moveTo(4.2f, 4.2f); lineTo(10.4f, 4.2f); lineTo(10.4f, 10.4f); lineTo(4.2f, 4.2f); close()
            moveTo(13.6f, 4.2f); lineTo(19.8f, 4.2f); lineTo(19.8f, 10.4f); lineTo(13.6f, 4.2f); close()
            moveTo(4.2f, 13.6f); lineTo(10.4f, 13.6f); lineTo(10.4f, 19.8f); lineTo(4.2f, 13.6f); close()
            moveTo(13.6f, 13.6f); lineTo(19.8f, 13.6f); lineTo(19.8f, 19.8f); lineTo(13.6f, 13.6f); close()
        }
    }

    /** 已完成（圆圈勾选） */
    val DoneCircle: ImageVector = createIcon("DoneCircle") {
        stroke {
            moveTo(3.4f, 12f)
            arcToRelative(8.6f, 8.6f, 0f, true, true, 17.2f, 0f)
            arcToRelative(8.6f, 8.6f, 0f, true, true, -17.2f, 0f)
            close()
        }
        stroke(width = 2f) {
            moveTo(7.8f, 12.2f); lineTo(10.9f, 15.3f); lineTo(16.4f, 8.8f)
        }
    }

    /** 设置（滑杆） */
    val Settings: ImageVector = createIcon("Settings") {
        stroke {
            moveTo(3.6f, 7.2f); lineTo(20.4f, 7.2f)
            moveTo(3.6f, 12f); lineTo(20.4f, 12f)
            moveTo(3.6f, 16.8f); lineTo(20.4f, 16.8f)
            moveTo(9f, 4.6f); lineTo(9f, 9.8f)
            moveTo(15f, 9.4f); lineTo(15f, 14.6f)
            moveTo(9f, 14.2f); lineTo(9f, 19.4f)
        }
    }

    /** 右箭头 */
    val ChevronRight: ImageVector = createIcon("ChevronRight") {
        stroke(width = 1.9f) {
            moveTo(9.4f, 5.6f); lineTo(15.6f, 12f); lineTo(9.4f, 18.4f)
        }
    }

    /** 左箭头（返回） */
    val ChevronLeft: ImageVector = createIcon("ChevronLeft") {
        stroke(width = 1.9f) {
            moveTo(14.6f, 5.6f); lineTo(8.4f, 12f); lineTo(14.6f, 18.4f)
        }
    }

    /** 优先级旗帜 */
    val Flag: ImageVector = createIcon("Flag") {
        stroke {
            moveTo(6.2f, 20.4f); lineTo(6.2f, 4f)
            moveTo(6.2f, 5f); lineTo(15f, 5f); lineTo(17.4f, 8f); lineTo(15f, 11f); lineTo(6.2f, 11f)
        }
    }

    /** 向上发送 */
    val Send: ImageVector = createIcon("Send") {
        stroke(width = 2f) {
            moveTo(12f, 19.4f); lineTo(12f, 5f)
            moveTo(5.8f, 11.2f); lineTo(12f, 5f); lineTo(18.2f, 11.2f)
        }
    }

    /** 项目（# 号） */
    val Hash: ImageVector = createIcon("Hash") {
        stroke(width = 1.6f) {
            moveTo(9.4f, 4.4f); lineTo(7.6f, 19.6f)
            moveTo(16.4f, 4.4f); lineTo(14.6f, 19.6f)
            moveTo(4.6f, 9f); lineTo(19.4f, 9f)
            moveTo(4f, 15f); lineTo(18.8f, 15f)
        }
    }

    /** 垃圾桶 */
    val Trash: ImageVector = createIcon("Trash") {
        stroke {
            moveTo(4.6f, 6.8f); lineTo(19.4f, 6.8f)
            moveTo(9.4f, 6.8f); lineTo(9.4f, 4.4f); lineTo(14.6f, 4.4f); lineTo(14.6f, 6.8f)
            moveTo(6.6f, 6.8f); lineTo(7.6f, 20f); lineTo(16.4f, 20f); lineTo(17.4f, 6.8f)
            moveTo(10.4f, 10.4f); lineTo(10.8f, 16.6f)
            moveTo(13.6f, 10.4f); lineTo(13.2f, 16.6f)
        }
    }

    /** 编辑（铅笔） */
    val Edit: ImageVector = createIcon("Edit") {
        stroke {
            moveTo(16.4f, 4.2f); lineTo(19.8f, 7.6f); lineTo(8.4f, 19f); lineTo(4.2f, 20f)
            lineTo(5.2f, 15.8f); close()
        }
    }
}
