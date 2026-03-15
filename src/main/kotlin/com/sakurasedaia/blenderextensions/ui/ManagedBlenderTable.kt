package com.sakurasedaia.blenderextensions.ui

import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.progress.Task
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.ui.table.JBTable
import com.sakurasedaia.blenderextensions.LangManager
import com.sakurasedaia.blenderextensions.blender.BlenderCommunicationService
import com.sakurasedaia.blenderextensions.blender.BlenderDownloader
import com.sakurasedaia.blenderextensions.blender.BlenderService
import com.sakurasedaia.blenderextensions.blender.BlenderVersions
import com.sakurasedaia.blenderextensions.icons.BlenderIcons
import com.sakurasedaia.blenderextensions.notifications.BlenderNotification
import java.awt.Component
import javax.swing.*
import javax.swing.table.AbstractTableModel
import javax.swing.table.TableCellRenderer

class ManagedBlenderTable(private val project: Project) : JBTable() {
    private val downloader = BlenderDownloader.getInstance(project)
    private val service = BlenderService.getInstance(project)
    private val commService = BlenderCommunicationService.getInstance(project)
    private val tableModel = ManagedBlenderTableModel()

    init {
        model = tableModel
        autoResizeMode = JTable.AUTO_RESIZE_LAST_COLUMN
        
        val buttonRenderer = ButtonRenderer()
        columnModel.getColumn(0).preferredWidth = 100
        
        columnModel.getColumn(1).cellRenderer = buttonRenderer
        columnModel.getColumn(1).cellEditor = ButtonEditor(JCheckBox())
        columnModel.getColumn(1).maxWidth = 150
        columnModel.getColumn(1).preferredWidth = 120
        
        columnModel.getColumn(2).cellRenderer = buttonRenderer
        columnModel.getColumn(2).cellEditor = ButtonEditor(JCheckBox())
        columnModel.getColumn(2).maxWidth = 80
        columnModel.getColumn(2).preferredWidth = 80
    }

    fun refresh() {
        tableModel.refresh()
    }

    private inner class ManagedBlenderTableModel : AbstractTableModel() {
        private val columnNames = arrayOf(
            LangManager.message("toolwindow.table.column.version"),
            LangManager.message("toolwindow.table.column.interpreter"),
            LangManager.message("toolwindow.table.column.action")
        )
        private val versions = BlenderVersions.SUPPORTED_VERSIONS

        override fun getRowCount(): Int = versions.size
        override fun getColumnCount(): Int = columnNames.size
        override fun getColumnName(column: Int): String = columnNames[column]

        override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
            val version = versions[rowIndex].majorMinor
            return when (columnIndex) {
                0 -> listOf("Blender", version).joinToString(" ")
                else -> ""
            }
        }

        override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean = columnIndex >= 1

        fun refresh() {
            downloader.clearCache()
            fireTableDataChanged()
        }
        
        fun getVersionAt(row: Int) = versions[row].majorMinor
    }

    private inner class ButtonRenderer : JButton(), TableCellRenderer {
        init {
            isOpaque = true
            border = BorderFactory.createEmptyBorder()
        }
        override fun getTableCellRendererComponent(
            table: JTable, value: Any, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int
        ): Component {
            if (column == 1) { // Interpreter
                val version = tableModel.getVersionAt(row)
                val downloaded = downloader.isDownloaded(version)
                icon = if (downloaded) BlenderIcons.Python else null
                toolTipText = if (downloaded) LangManager.message("toolwindow.setup.interpreter.tooltip") else null
                isVisible = downloaded
            } else { // Action
                text = value.toString()
                val version = tableModel.getVersionAt(row)
                icon = if (downloader.isDownloaded(version)) BlenderIcons.Remove else BlenderIcons.Install
            }
            return this
        }
    }

    private inner class ButtonEditor(checkBox: JCheckBox) : DefaultCellEditor(checkBox) {
        private val button = JButton()
        private var row = 0
        private var column = 0

        init {
            button.isOpaque = true
            button.border = BorderFactory.createEmptyBorder()
            button.addActionListener {
                val version = tableModel.getVersionAt(row)
                if (column == 1) { // Interpreter
                    val path = downloader.getOrDownloadBlenderPath(version)
                    if (path != null) {
                        service.setupPythonInterpreter(path)
                    }
                } else { // Action
                    if (downloader.isDownloaded(version)) {
                        val confirm = Messages.showYesNoDialog(
                            project,
                            LangManager.message("toolwindow.managed.action.delete.description"),
                            LangManager.message("toolwindow.managed.action.delete.title", version),
                            Messages.getQuestionIcon()
                        )
                        if (confirm == Messages.YES) {
                            if (!commService.isConnected()) {
                                downloader.deleteVersion(version)
                                tableModel.refresh()
                            } else {
                                BlenderNotification(project).sendError(
                                    title=LangManager.message("notification.delete.failed.title", "Blender $version"),
                                    content=LangManager.message("notification.delete.failed.reason.blender.running")
                                )
                                tableModel.refresh()
                            }
                        }
                    } else {
                        // Start download
                        ProgressManager.getInstance().run(
                            object : Task.Backgroundable(project, LangManager.message("action.download.blender.task", version)) {
                                override fun run(indicator: com.intellij.openapi.progress.ProgressIndicator) {
                                    downloader.getOrDownloadBlenderPath(version)
                                    SwingUtilities.invokeLater { tableModel.refresh() }
                                }
                            }
                        )
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
            val version = tableModel.getVersionAt(row)
            if (column == 1) {
                button.text = ""
                button.icon = if (downloader.isDownloaded(version)) BlenderIcons.Python else null
            } else {
                button.text = value.toString()
                button.icon = if (downloader.isDownloaded(version)) BlenderIcons.Remove else BlenderIcons.Install
            }
            return button
        }

        override fun getCellEditorValue(): Any = button.text
    }
}
