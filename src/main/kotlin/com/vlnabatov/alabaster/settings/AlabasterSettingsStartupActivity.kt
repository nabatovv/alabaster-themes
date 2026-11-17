package com.vlnabatov.alabaster.settings

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity

/**
 * Applies the stored colour overrides once the IDE has finished starting.
 * Without this, user-customised colours would only reappear after the settings
 * page was opened.
 */
class AlabasterSettingsStartupActivity : StartupActivity.DumbAware {
    override fun runActivity(project: Project) {
        ApplicationManager.getApplication().invokeLater {
            AlabasterSettingsService.getInstance().applyAll()
        }
    }
}
