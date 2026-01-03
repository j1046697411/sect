package cn.jzl.ecs.entity

import androidx.collection.mutableObjectListOf
import cn.jzl.core.list.ObjectFastList
import cn.jzl.ecs.World

class BatchEntityEditorPool(private val world: World) {

    private val editors = mutableObjectListOf<BatchEntityEditor>()

    fun obtain(entity: Entity): BatchEntityEditor {
        val editor = if (editors.isNotEmpty()) {
            editors.removeAt(editors.size - 1)
        } else {
            BatchEntityEditor(world, entity)
        }
        editor.entity = entity
        return editor
    }

    fun release(editor: BatchEntityEditor) {
        editors.add(editor)
    }
}