package com.sakurasedaia.blenderextensions.ui

import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import com.sakurasedaia.blenderextensions.BlenderBundle
import com.sakurasedaia.blenderextensions.blender.*
import com.sakurasedaia.blenderextensions.icons.BlenderIcons
import com.sakurasedaia.blenderextensions.settings.BlenderSettings
import java.awt.BorderLayout
import java.awt.Component
import java.awt.FlowLayout
import java.awt.Font
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import java.awt.event.ActionEvent
import java.nio.file.Path
import javax.swing.*
import javax.swing.table.AbstractTableModel
import javax.swing.table.DefaultTableCellRenderer
import javax.swing.table.TableCellEditor
import javax.swing.table.TableCellRenderer
class BlenderToolWindowContent(private val project: Project) {
    private val downloader = BlenderDownloader.getInstance(project)
    private val service = BlenderService.getInstance(project)
    private val communicationService = BlenderCommunicationService.getInstance(project)
    private val tableModel = BlenderVersionsTableModel()
    private val table = JBTable(tableModel)
    private val systemTableModel = SystemBlenderTableModel()
    private val systemTable = JBTable(systemTableModel)
    private val timer: Timer

    init {
        configureTable(table, true)
        configureTable(systemTable, false)
        
        timer = Timer(2000) {
            tableModel.fireTableDataChanged()
            systemTableModel.refresh()
        }
        timer.start()
    }

    private fun configureTable(table: JBTable, isManaged: Boolean) {
        if (!isManaged) {
            table.columnModel.getColumn(0).maxWidth = 60
            table.columnModel.getColumn(0).cellRenderer = object : DefaultTableCellRenderer() {
                override fun getTableCellRendererComponent(
                    table: JTable, value: Any, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int
                ): Component {
                    val component = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
                    if (value is Icon) {
                        icon = value
                        text = ""
                        horizontalAlignment = SwingConstants.CENTER
                        
                        val inst = (table.model as? SystemBlenderTableModel)?.getInstallationAt(row)
                        toolTipText = if (inst?.isCustom == true) BlenderBundle.message("toolwindow.table.tooltip.added.manually") else BlenderBundle.message("toolwindow.table.tooltip.auto.detected")
                    } else if (value is String) {
                        icon = null
                        text = value
                        foreground = if (value == BlenderBundle.message("toolwindow.table.status.detected")) JBUI.CurrentTheme.Label.foreground()
                        else UIUtil.getLabelDisabledForeground()
                    }
                    return component
                }
            }
            
            // Setup the remove button in the 4th column (index 3)
            val systemButtonRenderer = SystemButtonRenderer()
            val systemButtonEditor = SystemButtonEditor()
            table.columnModel.getColumn(3).cellRenderer = systemButtonRenderer
            table.columnModel.getColumn(3).cellEditor = systemButtonEditor
            table.columnModel.getColumn(3).maxWidth = 60
        }

        if (isManaged) {
            val buttonRenderer = ButtonRenderer()
            table.columnModel.getColumn(1).cellRenderer = buttonRenderer
            table.columnModel.getColumn(1).cellEditor = ButtonEditor(JCheckBox())
        }
    }

    fun getContent(): JComponent {
        val managedVersionsLabel = JBLabel(BlenderBundle.message("toolwindow.managed.versions.label")).apply {
            font = font.deriveFont(java.awt.Font.BOLD)
        }

        val refreshButton = JButton("", BlenderIcons.Refresh).apply {
            toolTipText = BlenderBundle.message("toolwindow.refresh.tooltip")
            addActionListener {
                tableModel.refresh()
                systemTableModel.refresh()
            }
        }
        
        val managedVersionsHeader = JPanel(BorderLayout()).apply {
            val leftPanel = JPanel(FlowLayout(FlowLayout.LEFT, 5, 0))
            leftPanel.add(managedVersionsLabel)
            add(leftPanel, BorderLayout.WEST)
            add(refreshButton, BorderLayout.EAST)
        }

        val systemVersionsLabel = JBLabel(BlenderBundle.message("toolwindow.system.versions.label")).apply {
            font = font.deriveFont(Font.BOLD)
        }

        val addCustomButton = JButton("", BlenderIcons.Add).apply {
            toolTipText = BlenderBundle.message("toolwindow.add.custom.tooltip")
            addActionListener {
                val descriptor = FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor()
                    .withTitle(BlenderBundle.message("toolwindow.select.blender.title"))
                    .withDescription(BlenderBundle.message("toolwindow.select.blender.description"))
                
                val file = FileChooser.chooseFile(descriptor, project, null)
                if (file != null) {
                    BlenderSettings.getInstance(project).addCustomBlenderPath(file.path)
                    systemTableModel.refresh()
                }
            }
        }

        val systemVersionsHeader = JPanel(BorderLayout()).apply {
            add(systemVersionsLabel, BorderLayout.WEST)
            add(addCustomButton, BorderLayout.EAST)
        }

        val sandboxLabel = JBLabel(BlenderBundle.message("toolwindow.sandbox.management.label")).apply {
            font = font.deriveFont(java.awt.Font.BOLD)
        }
        
        val clearSandboxButton = JButton(BlenderBundle.message("toolwindow.clear.sandbox.button"), BlenderIcons.Remove).apply {
            addActionListener {
                val confirm = Messages.showYesNoDialog(
                    project,
                    BlenderBundle.message("toolwindow.clear.sandbox.confirm.message"),
                    BlenderBundle.message("toolwindow.clear.sandbox.confirm.title"),
                    Messages.getQuestionIcon()
                )
                if (confirm == Messages.YES) {
                    service.clearSandbox()
                    Messages.showInfoMessage(project, BlenderBundle.message("toolwindow.clear.sandbox.success.message"), BlenderBundle.message("toolwindow.clear.sandbox.confirm.title"))
                }
            }
        }
        
        // Use GridBagLayout for vertical expandability of the table lists
        val mainPanel = JPanel(GridBagLayout())
        val c = GridBagConstraints().apply {
            fill = GridBagConstraints.BOTH
            gridx = 0
            weightx = 1.0
        }

        // Managed Header
        c.gridy = 0
        c.weighty = 0.0
        c.insets = JBUI.insets(5)
        mainPanel.add(managedVersionsHeader, c)

        // Managed Table (Expandable)
        c.gridy = 1
        c.weighty = 1.0
        c.insets = JBUI.insets(0, 5, 5, 5)
        mainPanel.add(JBScrollPane(table).apply {
            preferredSize = JBUI.size(400, 150)
            minimumSize = JBUI.size(400, 100)
        }, c)

        // System Label
        c.gridy = 2
        c.weighty = 0.0
        c.insets = JBUI.insets(15, 5, 5, 5)
        mainPanel.add(systemVersionsHeader, c)

        // System Table (Expandable)
        c.gridy = 3
        c.weighty = 1.0
        c.insets = JBUI.insets(0, 5, 5, 5)
        mainPanel.add(JBScrollPane(systemTable).apply {
            preferredSize = JBUI.size(400, 150)
            minimumSize = JBUI.size(400, 100)
        }, c)

        // Sandbox Section
        c.gridy = 4
        c.weighty = 0.0
        c.insets = JBUI.insets(20, 5, 5, 5)
        mainPanel.add(sandboxLabel, c)

        c.gridy = 5
        c.insets = JBUI.insets(0, 5, 5, 5)
        mainPanel.add(clearSandboxButton, c)

        // Wrap in a BorderLayout panel to respect expansion
        val panel = JPanel(BorderLayout())
        panel.add(mainPanel, BorderLayout.CENTER)
        panel.border = JBUI.Borders.empty(5)
        
        return panel
    }

    private inner class BlenderVersionsTableModel : AbstractTableModel() {
        private val columnNames = arrayOf(
            BlenderBundle.message("toolwindow.table.column.version"),
            BlenderBundle.message("toolwindow.table.column.action")
        )
        private val versions = BlenderVersions.SUPPORTED_VERSIONS

        override fun getRowCount(): Int = versions.size
        override fun getColumnCount(): Int = columnNames.size
        override fun getColumnName(column: Int): String = columnNames[column]

        override fun getValueAt(rowIndex: Int, columnIndex: Int): Any {
            val version = versions[rowIndex]
            return when (columnIndex) {
                0 -> listOf("Blender", version).joinToString(" ")
                else -> ""
            }
        }

        override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean = columnIndex == 1

        fun refresh() {
            downloader.clearCache()
            fireTableDataChanged()
        }
        
        fun getVersionAt(row: Int) = versions[row]
    }

    private inner class SystemBlenderTableModel : AbstractTableModel() {
        private val columnNames = arrayOf(
            BlenderBundle.message("toolwindow.table.column.status"),
            BlenderBundle.message("toolwindow.table.column.name"),
            BlenderBundle.message("toolwindow.table.column.path"),
            BlenderBundle.message("toolwindow.table.column.action")
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
                2 -> inst.path
                3 -> if (inst.isCustom) BlenderBundle.message("toolwindow.table.action.remove") else ""
                else -> ""
            }
        }

        override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean {
            val inst = installations[rowIndex]
            return (columnIndex == 1 && inst.isCustom) || columnIndex == 3
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
                installations = BlenderScanner.scanSystemInstallations(force = true, customPaths = customPaths)
                SwingUtilities.invokeLater { fireTableDataChanged() }
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
            val inst = systemTableModel.getInstallationAt(row)
            if (inst.isCustom) {
                text = ""
                icon = BlenderIcons.Remove
                isVisible = true
            } else {
                text = ""
                icon = null
                isVisible = false
            }
            return this
        }
    }

    private inner class SystemButtonEditor : AbstractCellEditor(), TableCellEditor {
        private val button = JButton()
        private var row = 0

        init {
            button.isOpaque = true
            button.border = BorderFactory.createEmptyBorder()
            button.addActionListener {
                val inst = systemTableModel.getInstallationAt(row)
                if (inst.isCustom && inst.originPath != null) {
                    val confirm = Messages.showYesNoDialog(
                        project,
                        BlenderBundle.message("toolwindow.table.action.delete.confirm.message"),
                        BlenderBundle.message("toolwindow.table.action.delete.confirm.title"),
                        Messages.getQuestionIcon()
                    )
                    if (confirm == Messages.YES) {
                        val settings = BlenderSettings.getInstance(project)
                        settings.removeCustomBlenderPath(inst.originPath)
                        systemTableModel.refresh()
                    }
                }
                fireEditingStopped()
            }
        }

        override fun getTableCellEditorComponent(
            table: JTable, value: Any, isSelected: Boolean, row: Int, column: Int
        ): Component {
            this.row = row
            val inst = systemTableModel.getInstallationAt(row)
            if (inst.isCustom) {
                button.icon = BlenderIcons.Remove
                button.isVisible = true
            } else {
                button.icon = null
                button.isVisible = false
            }
            return button
        }

        override fun getCellEditorValue(): Any = ""
    }

    private inner class ButtonRenderer : JButton(), TableCellRenderer {
        init {
            isOpaque = true
            border = BorderFactory.createEmptyBorder()
        }
        override fun getTableCellRendererComponent(
            table: JTable, value: Any, isSelected: Boolean, hasFocus: Boolean, row: Int, column: Int
        ): Component {
            text = value.toString()
            val version = tableModel.getVersionAt(row)
            icon = if (downloader.isDownloaded(version)) BlenderIcons.Remove else BlenderIcons.Install
            return this
        }
    }

    private inner class ButtonEditor(checkBox: JCheckBox) : DefaultCellEditor(checkBox) {
        private val button = JButton()
        private var row = 0

        init {
            button.isOpaque = true
            button.border = BorderFactory.createEmptyBorder()
            button.addActionListener {
                val version = tableModel.getVersionAt(row)
                if (downloader.isDownloaded(version)) {
                    val confirm = Messages.showYesNoDialog(
                        project,
                        BlenderBundle.message("toolwindow.table.action.delete.version.confirm.message", version),
                        BlenderBundle.message("toolwindow.table.action.delete.version.confirm.title"),
                        Messages.getQuestionIcon()
                    )
                    if (confirm == Messages.YES) {
                        downloader.deleteVersion(version)
                        tableModel.refresh()
                    }
                } else {
                    // Start download
                    com.intellij.openapi.progress.ProgressManager.getInstance().run(
                        object : com.intellij.openapi.progress.Task.Backgroundable(project, BlenderBundle.message("toolwindow.table.action.downloading.task", version)) {
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
            val version = tableModel.getVersionAt(row)
            button.icon = if (downloader.isDownloaded(version)) BlenderIcons.Remove else BlenderIcons.Install
            return button
        }

        override fun getCellEditorValue(): Any = button.text
    }
}
