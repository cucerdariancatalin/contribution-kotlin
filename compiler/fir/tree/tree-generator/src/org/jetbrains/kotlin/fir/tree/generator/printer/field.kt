/*
 * Copyright 2010-2020 JetBrains s.r.o. and Kotlin Programming Language contributors.
 * Use of this source code is governed by the Apache 2.0 license that can be found in the license/LICENSE.txt file.
 */

package org.jetbrains.kotlin.fir.tree.generator.printer

import org.jetbrains.kotlin.fir.tree.generator.model.Field
import org.jetbrains.kotlin.fir.tree.generator.model.SimpleField
import org.jetbrains.kotlin.util.SmartPrinter


fun SmartPrinter.printField(field: Field, isImplementation: Boolean, override: Boolean, end: String) {
    if (isImplementation && !field.isVal && field.isVolatile) {
        println("@Volatile")
    }
    if (override) {
        print("override ")
    }
    if (!isImplementation || field.isVal) {
        print("val")
    } else {
        print("var")
    }
    val type = if (isImplementation) field.getMutableType() else field.typeWithArguments
    println(" ${field.name}: $type$end")
}

fun SmartPrinter.printFieldWithDefaultInImplementation(field: Field) {
    if (!field.isVal && field.isVolatile) {
        println("@Volatile")
    }
    val defaultValue = field.defaultValueInImplementation
    print("override ")
    if (field.isVal) {
        print("val")
    } else {
        print("var")
    }
    print(" ${field.name}: ${field.getMutableType()} ")
    if (field.withGetter) {
        if (field.customSetter != null) {
            println()
            pushIndent()
        }
        print("get() ")
    }
    requireNotNull(defaultValue) {
        "No default value for $field"
    }
    println("= $defaultValue")
    field.customSetter?.let {
        println("set(value) {")
        pushIndent()
        println(it)
        popIndent()
        println("}")
        popIndent()
    }
}
