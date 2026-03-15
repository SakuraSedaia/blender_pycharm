package com.sakurasedaia.blenderextensions.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.ui.table.JBTable
import com.sakurasedaia.blenderextensions.LangManager
import com.sakurasedaia.blenderextensions.blender.BlenderInstallation
import com.sakurasedaia.blenderextensions.blender.BlenderScanner
import com.sakurasedaia.blenderextensions.blender.BlenderService
import com.sakurasedaia.blenderextensions.icons.BlenderIcons
import com.sakurasedaia.blenderextensions.settings.BlenderSettings
import java.awt.Component
import javax.swing.*
import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableCellEditor
import javax.swing.table.TableCellRenderer

class SystemBlenderTable(private val project: Project) : JBTable() {
    private val service = BlenderService.getInstance(project)
    private val tableModel = SystemBlenderTableModel()

    init {
        model = tableModel
        autoResizeMode = JTable.AUTO_RESIZE_LAST_COLUMN
        
        val systemButtonRenderer = SystemButtonRenderer()
        val systemButtonEditor = SystemButtonEditor()
        
        columnModel.getColumn(0).maxWidth = 60
        columnModel.getColumn(0).preferredWidth = 40
        columnModel.getColumn(1).preferredWidth = 100
        columnModel.getColumn(2).maxWidth = 150
        columnModel.getColumn(2).preferredWidth = 120
        columnModel.getColumn(3).maxWidth = 80
        columnModel.getColumn(3).preferredWidth = 80

        columnModel.getColumn(0).cellRenderer = object : DefaultTableCellRenderer() {
            override fun getTableCellRendererComponent(
                table: JTable, value: Any, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int
            ): Component {
                val component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
                if (value is Icon) {
                    icon = value
                    text = ""
                    horizontalAlignment = SwingConstants.CENTER
                    
                    val inst = tableModel.getInstallationAt(row)
                    toolTipText = if (inst.isCustom) LangManager.message("toolwindow.system.table.status.manual.tooltip") else LangManager.message("toolwindow.system.table.status.auto.tooltip")
                }
                return component
            }
        }
        
        // Setup the interpreter button
        columnModel.getColumn(2).cellRenderer = systemButtonRenderer
        columnModel.getColumn(2).cellEditor = systemButtonEditor

        // Setup the remove button in the 4th column (index 3)
        columnModel.getColumn(3).cellRenderer = systemButtonRenderer
        columnModel.getColumn(3).cellEditor = systemButtonEditor
    }

    fun getSelectedInstallation(): BlenderInstallation? {
        val selectedRow = selectedRow
        if (selectedRow != -1) {
            return tableModel.getInstallationAt(convertRowIndexToModel(selectedRow))
        }
        return null
    }

    fun refresh() {
        tableModel.refresh()
    }

    private inner class SystemBlenderTableModel : AbstractTableModel() {
        private val columnNames = arrayOf(
            LangManager.message("toolwindow.system.table.column.status"),
            LangManager.message("toolwindow.table.column.version"),
            LangManager.message("toolwindow.table.column.interpreter"),
            LangManager.message("toolwindow.table.column.action")
        )
        private var installations = listOf<BlenderInstallation>()

        init {
            refresh()
        }

        override fun getRowCount(): Int = installations.size
        override fun getColumnCount(): Int = columnNames.size
        override fun getColumnName(column: Int): String = columnNames[column]

        override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
            val inst = installations[rowIndex]
            return when (columnIndex) {
                0 -> if (inst.isCustom) BlenderIcons.Custom else BlenderIcons.System
                1 -> inst.name
                3 -> if (inst.isCustom) LangManager.message("toolwindow.table.action.remove") else ""
                else -> ""
            }
        }

        override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
            val inst = installations[rowIndex]
            return (columnIndex == 1 && inst.isCustom) || columnIndex >= 2
        }

        override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {
            if (columnIndex == 1) {
                val inst = installations[rowIndex]
                if (inst.isCustom && inst.originPath != null) {
                    val settings = BlenderSettings.getInstance(project)
                    settings.addCustomBlenderPath(inst.originPath, aValue.toString())
                    refresh()
                }
            }
        }

        fun refresh() {
            com.intellij.openapi.application.ApplicationManager.getApplication().executeOnPooledThread {
                val customPaths = BlenderSettings.getInstance(project).getCustomBlenderPaths()
                val newInstallations = BlenderScanner.scanSystemInstallations(force = true, customPaths = customPaths)
                SwingUtilities.invokeLater {
                    // Check if the project is not disposed before updating UI
                    if (!project.isDisposed) {
                        installations = newInstallations
                        fireTableDataChanged()
                    }
                }
            }
        }

        fun getInstallationAt(row: Int) = installations[row]
    }

    private inner class SystemButtonRenderer : JButton(), TableCellRenderer {
        init {
            isOpaque = true
            border = BorderFactory.createEmptyBorder()
        }
        override fun getTableCellRendererComponent(
            table: JTable, value: Any, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int
        ): Component {
            val inst = tableModel.getInstallationAt(row)
            if (column == 2) { // Interpreter column
                icon = BlenderIcons.Python
                toolTipText = LangManager.message("toolwindow.setup.interpreter.tooltip")
                isVisible = true
            } else if (column == 3) { // Action column
                if (inst.isCustom) {
                    text = ""
                    icon = BlenderIcons.Remove
                    isVisible = true
                } else {
                    text = ""
                    icon = null
                    isVisible = false
                }
            }
            return this
        }
    }

    private inner class SystemButtonEditor : AbstractCellEditor(), TableCellEditor {
        private val button = JButton()
        private var row = 0
        private var column = 0

        init {
            button.isOpaque = true
            button.border = BorderFactory.createEmptyBorder()
            button.addActionListener {
                val inst = tableModel.getInstallationAt(row)
                if (column == 2) { // Interpreter
                    service.setupPythonInterpreter(inst.path)
                } else if (column == 3) { // Remove
                    if (inst.isCustom && inst.originPath != null) {
                        val confirm = Messages.showYesNoDialog(
                            project,
                            LangManager.message("toolwindow.system.table.action.delete.confirm.message"),
                            LangManager.message("toolwindow.system.table.action.delete.confirm.button"),
                            Messages.getQuestionIcon()
                        )
                        if (confirm == Messages.YES) {
                            val settings = BlenderSettings.getInstance(project)
                            settings.removeCustomBlenderPath(inst.originPath)
                            tableModel.refresh()
                        }
                    }
                }
                fireEditingStopped()
            }
        }

        override fun getTableCellEditorComponent(
            table: JTable, value: Any, isSelected: Boolean, row: Int, column: Int
        ): Component {
            this.row = row
            this.column = column
            val inst = tableModel.getInstallationAt(row)
            if (column == 2) {
                button.icon = BlenderIcons.Python
                button.isVisible = true
            } else if (column == 3) {
                if (inst.isCustom) {
                    button.icon = BlenderIcons.Remove
                    button.isVisible = true
                } else {
                    button.icon = null
                    button.isVisible = false
                }
            }
            return button
        }

        override fun getCellEditorValue(): Any = ""
    }
}
