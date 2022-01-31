/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.analysis.low.level.api.fir.diagnostics

import org.jetbrains.kotlin.fir.analysis.collectors.CheckerRunningDiagnosticCollectorVisitor
import org.jetbrains.kotlin.fir.analysis.collectors.components.AbstractDiagnosticCollectorComponent
import org.jetbrains.kotlin.fir.declarations.FirDeclaration
import org.jetbrains.kotlin.analysis.low.level.api.fir.diagnostics.fir.LLFirStructureElementDiagnosticsCollector

internal class FileStructureElementDiagnosticsCollector private constructor(private val useExtendedCheckers: Boolean) {
    companion object {
        val USUAL_COLLECTOR = FileStructureElementDiagnosticsCollector(useExtendedCheckers = false)
        val EXTENDED_COLLECTOR = FileStructureElementDiagnosticsCollector(useExtendedCheckers = true)
    }

    fun collectForStructureElement(
        firDeclaration: FirDeclaration,
        createVisitor: (components: List<AbstractDiagnosticCollectorComponent>) -> CheckerRunningDiagnosticCollectorVisitor,
    ): FileStructureElementDiagnosticList {
        val reporter = LLFirDiagnosticReporter()
        val collector = LLFirStructureElementDiagnosticsCollector(
            firDeclaration.moduleData.session,
            createVisitor,
            useExtendedCheckers,
        )
        collector.collectDiagnostics(firDeclaration, reporter)
        return FileStructureElementDiagnosticList(reporter.diagnostics)
    }
}
