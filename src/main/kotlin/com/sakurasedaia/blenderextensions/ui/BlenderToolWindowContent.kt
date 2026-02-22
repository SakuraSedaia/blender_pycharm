package com.sakurasedaia.blenderextensions.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.FormBuilder
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.sakurasedaia.blenderextensions.blender.BlenderDownloader
import com.sakurasedaia.blenderextensions.blender.BlenderService
import com.sakurasedaia.blenderextensions.blender.BlenderVersions
import java.awt.BorderLayout
import java.awt.Component
import java.awt.event.ActionEvent
import javax.swing.*
import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableCellEditor
import javax.swing.table.TableCellRenderer
class BlenderToolWindowContent(private val project: Project) {
    private val downloader = BlenderDownloader.getInstance(project)
    private val service = BlenderService.getInstance(project)
    private val tableModel = BlenderVersionsTableModel()
    private val table = JBTable(tableModel)

    init {
        table.columnModel.getColumn(1).cellRenderer = object : DefaultTableCellRenderer() {
            override fun getTableCellRendererComponent(
                table: JTable, value: Any, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int
            ): Component {
                val component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
                if (value is String) {
                    foreground = if (value == "Downloaded") JBUI.CurrentTheme.Label.foreground()
                                else UIUtil.getLabelDisabledForeground()
                }
                return component
            }
        }

        val buttonRenderer = ButtonRenderer()
        table.columnModel.getColumn(2).cellRenderer = buttonRenderer
        table.columnModel.getColumn(2).cellEditor = ButtonEditor(JCheckBox())
    }

    fun getContent(): JComponent {
        val panel = JPanel(BorderLayout())
        
        val managedVersionsLabel = JBLabel("Managed Blender Installations").apply {
            font = font.deriveFont(java.awt.Font.BOLD)
        }

        val refreshButton = JButton("Refresh Status").apply {
            addActionListener { tableModel.refresh() }
        }
        
        val managedVersionsHeader = JPanel(BorderLayout()).apply {
            add(managedVersionsLabel, BorderLayout.WEST)
            add(refreshButton, BorderLayout.EAST)
        }
        
        val managedVersionsPanel = FormBuilder.createFormBuilder()
            .addComponent(managedVersionsHeader)
            .addComponent(JBScrollPane(table))
            .panel

        val sandboxLabel = JBLabel("Project Sandbox Management").apply {
            font = font.deriveFont(java.awt.Font.BOLD)
        }
        
        val clearSandboxButton = JButton("Clear Sandbox (.blender_sandbox)").apply {
            addActionListener {
                val confirm = Messages.showYesNoDialog(
                    project,
                    "Are you sure you want to delete the .blender_sandbox directory? This will remove all local configuration and scripts for this project's sandbox.",
                    "Clear Sandbox",
                    Messages.getQuestionIcon()
                )
                if (confirm == Messages.YES) {
                    service.clearSandbox()
                    Messages.showInfoMessage(project, "Sandbox cleared successfully.", "Clear Sandbox")
                }
            }
        }
        
        val sandboxPanel = FormBuilder.createFormBuilder()
            .addComponent(sandboxLabel)
            .addComponent(clearSandboxButton)
            .panel

        val mainPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)
            add(managedVersionsPanel)
            add(Box.createVerticalStrut(20))
            add(sandboxPanel)
            add(Box.createVerticalGlue())
        }
        
        panel.add(mainPanel, BorderLayout.NORTH)
        return panel
    }

    private inner class BlenderVersionsTableModel : AbstractTableModel() {
        private val columnNames = arrayOf("Version", "Status", "Action")
        private val versions = BlenderVersions.SUPPORTED_VERSIONS

        override fun getRowCount(): Int = versions.size
        override fun getColumnCount(): Int = columnNames.size
        override fun getColumnName(column: Int): String = columnNames[column]

        override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
            val version = versions[rowIndex]
            return when (columnIndex) {
                0 -> version
                1 -> if (downloader.isDownloaded(version)) "Downloaded" else "Not downloaded"
                2 -> if (downloader.isDownloaded(version)) "Delete" else "Download"
                else -> ""
            }
        }

        override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean = columnIndex == 2

        fun refresh() {
            downloader.clearCache()
            fireTableDataChanged()
        }
        
        fun getVersionAt(row: Int) = versions[row]
    }

    private inner class ButtonRenderer : JButton(), TableCellRenderer {
        init {
            isOpaque = true
        }
        override fun getTableCellRendererComponent(
            table: JTable, value: Any, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int
        ): Component {
            text = value.toString()
            return this
        }
    }

    private inner class ButtonEditor(checkBox: JCheckBox) : DefaultCellEditor(checkBox) {
        private val button = JButton()
        private var row = 0

        init {
            button.isOpaque = true
            button.addActionListener {
                val version = tableModel.getVersionAt(row)
                if (downloader.isDownloaded(version)) {
                    val confirm = Messages.showYesNoDialog(
                        project,
                        "Are you sure you want to delete Blender $version?",
                        "Delete Blender",
                        Messages.getQuestionIcon()
                    )
                    if (confirm == Messages.YES) {
                        downloader.deleteVersion(version)
                        tableModel.refresh()
                    }
                } else {
                    // Start download
                    com.intellij.openapi.progress.ProgressManager.getInstance().run(
                        object : com.intellij.openapi.progress.Task.Backgroundable(project, "Downloading Blender $version") {
                            override fun run(indicator: com.intellij.openapi.progress.ProgressIndicator) {
                                downloader.getOrDownloadBlenderPath(version)
                                SwingUtilities.invokeLater { tableModel.refresh() }
                            }
                        }
                    )
                }
                fireEditingStopped()
            }
        }

        override fun getTableCellEditorComponent(
            table: JTable, value: Any, isSelected: Boolean, row: Int, column: Int
        ): Component {
            this.row = row
            button.text = value.toString()
            return button
        }

        override fun getCellEditorValue(): Any = button.text
    }
}
