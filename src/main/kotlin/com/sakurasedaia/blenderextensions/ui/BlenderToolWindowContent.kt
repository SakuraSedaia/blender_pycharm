package com.sakurasedaia.blenderextensions.ui

import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import com.sakurasedaia.blenderextensions.LangManager
import com.sakurasedaia.blenderextensions.blender.*
import com.sakurasedaia.blenderextensions.icons.BlenderIcons
import com.sakurasedaia.blenderextensions.notifications.BlenderNotification
import com.sakurasedaia.blenderextensions.settings.BlenderSettings
import java.awt.*
import javax.swing.*

class BlenderToolWindowContent(private val project: Project) {
    private val service = BlenderService.getInstance(project)
    private val commService = BlenderCommunicationService.getInstance(project)
    private val managedTable = ManagedBlenderTable(project)
    private val systemTable = SystemBlenderTable(project)
    private val systemPathField = JBTextField().apply {
        isEditable = false
    }
    private val timer: Timer

    init {
        timer = Timer(5000) {
            if (!project.isDisposed) {
                managedTable.refresh()
                systemTable.refresh()
            } else {
                (it.source as Timer).stop()
            }
        }
        timer.start()

        systemTable.selectionModel.addListSelectionListener {
            if (!it.valueIsAdjusting) {
                val inst = systemTable.getSelectedInstallation()
                systemPathField.text = inst?.path ?: ""
            }
        }
    }

    fun getContent(): JComponent {
        val managedVersionsLabel = JBLabel(LangManager.message("toolwindow.managed.table.title")).apply {
            font = font.deriveFont(Font.BOLD)
        }

        val refreshButton = JButton("", BlenderIcons.Refresh).apply {
            toolTipText = LangManager.message("toolwindow.refresh.tooltip")
            addActionListener {
                managedTable.refresh()
                systemTable.refresh()
            }
        }
        
        val managedVersionsHeader = JPanel(BorderLayout()).apply {
            val leftPanel = JPanel(FlowLayout(FlowLayout.LEFT, 5, 0))
            leftPanel.add(managedVersionsLabel)
            add(leftPanel, BorderLayout.WEST)
            add(refreshButton, BorderLayout.EAST)
        }

        val systemVersionsLabel = JBLabel(LangManager.message("toolwindow.system.table.title")).apply {
            font = font.deriveFont(Font.BOLD)
        }

        val addCustomButton = JButton("", BlenderIcons.Add).apply {
            toolTipText = LangManager.message("toolwindow.system.table.add.custom.tooltip")
            addActionListener {
                val descriptor = FileChooserDescriptorFactory.createSingleFileOrFolderDescriptor()
                    .withTitle(LangManager.message("toolwindow.system.modal.select.title"))
                    .withDescription(LangManager.message("toolwindow.system.modal.select.description"))
                
                val file = FileChooser.chooseFile(descriptor, project, null)
                if (file != null) {
                    BlenderSettings.getInstance(project).addCustomBlenderPath(file.path)
                    systemTable.refresh()
                }
            }
        }

        val systemVersionsHeader = JPanel(BorderLayout()).apply {
            add(systemVersionsLabel, BorderLayout.WEST)
            add(addCustomButton, BorderLayout.EAST)
        }

        val sandboxLabel = JBLabel(LangManager.message("toolwindow.sandbox.management.label")).apply {
            font = font.deriveFont(Font.BOLD)
        }
        
        val clearSandboxButton = JButton(LangManager.message("toolwindow.sandbox.clear"), BlenderIcons.Remove).apply {
            addActionListener {
                val confirm = Messages.showYesNoDialog(
                    project,
                    LangManager.message("toolwindow.sandbox.clear.warning"),
                    LangManager.message("toolwindow.sandbox.clear.confirm"),
                    Messages.getQuestionIcon()
                )
                if (confirm == Messages.YES) {
                    if (!commService.isConnected()) {
                        service.clearSandbox()
                        Messages.showInfoMessage(
                            project,
                            LangManager.message("toolwindow.sandbox.clear.success"),
                            LangManager.message("toolwindow.sandbox.clear.success.title")
                        )
                    }
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
        mainPanel.add(JBScrollPane(managedTable), c)

        // System Label
        c.gridy = 2
        c.weighty = 0.0
        c.insets = JBUI.insets(15, 5, 5, 5)
        mainPanel.add(systemVersionsHeader, c)

        // System Table (Expandable)
        c.gridy = 3
        c.weighty = 1.0
        c.insets = JBUI.insets(0, 5, 5, 5)
        mainPanel.add(JBScrollPane(systemTable), c)

        // System Path Field
        c.gridy = 4
        c.weighty = 0.0
        c.insets = JBUI.insets(5, 5, 5, 5)
        mainPanel.add(systemPathField, c)

        // Sandbox Section
        c.gridy = 5
        c.weighty = 0.0
        c.insets = JBUI.insets(20, 5, 5, 5)
        mainPanel.add(sandboxLabel, c)

        c.gridy = 6
        c.insets = JBUI.insets(0, 5, 5, 5)
        mainPanel.add(clearSandboxButton, c)

        // Wrap in a BorderLayout panel to respect expansion
        val panel = JPanel(BorderLayout())
        panel.add(mainPanel, BorderLayout.CENTER)
        panel.border = JBUI.Borders.empty(5)
        
        return panel
    }
}
