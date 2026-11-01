/*
 * Pluto TV 17 phone build — NaN corner crash fix
 *
 * On a TV-sized screen (window width >= 600dp) the phone build's series page crashes with
 *   IllegalArgumentException: Corner size in Px can't be negative(topStart = NaN, ...)
 * from Compose's CornerBasedShape.createOutline, an app bug that has nothing to do with the ad patches
 * (a Clone-only build crashes the same way; the phone layout at < 600dp does not). createOutline asks each of
 * its four CornerSize objects for its px value (an interface call returning float) and then requires all
 * four to be >= 0, which a NaN fails. Here each of those four results goes through
 * PlutoComposeFix.nanToZero first, so a NaN radius becomes a square corner and the page opens.
 */

package ajstrick81.morphe.patches.pluto17

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import ajstrick81.morphe.patches.pluto.shared.Constants
import com.android.tools.smali.dexlib2.Opcode
import com.android.tools.smali.dexlib2.iface.instruction.OneRegisterInstruction
import com.android.tools.smali.dexlib2.iface.instruction.ReferenceInstruction
import com.android.tools.smali.dexlib2.iface.reference.MethodReference

private const val FIX = "Lajstrick81/morphe/extension/pluto17/PlutoComposeFix;"

@Suppress("unused")
val pluto17ComposeCornerFixPatch = bytecodePatch(
    name = "Fix corner crash (Pluto TV 17 phone build)",
    description = "Stops the Pluto TV 17 phone build from crashing with \"Corner size in Px can't be negative " +
        "(… = NaN)\" when a series page opens on a TV-sized screen: a corner radius that works out to NaN becomes " +
        "a square corner. Only for the phone/tablet build; fail-open (does nothing if the code isn't found). " +
        "Opt-in.",
    default = false,
) {
    compatibleWith(Constants.COMPATIBILITY_17)

    extendWith("extensions/extension.mpe")

    execute {
        val method = CornerOutlineFingerprint.methodOrNull ?: return@execute

        // The four `invoke-interface {vX, size, density}, CornerSize.toPx(...)F` + `move-result vN` pairs come first.
        val results = mutableListOf<Pair<Int, Int>>() // (index of move-result, register)
        val instructions = method.implementation!!.instructions.toList()
        for (i in 0 until instructions.size - 1) {
            val ins = instructions[i]
            if (ins.opcode != Opcode.INVOKE_INTERFACE) continue
            val ref = (ins as ReferenceInstruction).reference as? MethodReference ?: continue
            if (ref.returnType != "F") continue
            val next = instructions[i + 1]
            if (next.opcode != Opcode.MOVE_RESULT) continue
            results += (i + 1) to (next as OneRegisterInstruction).registerA
            if (results.size == 4) break
        }
        if (results.size != 4 || results.any { it.second > 15 }) return@execute

        // Back to front so the earlier indexes stay valid.
        results.reversed().forEach { (index, register) ->
            method.addInstructions(
                index + 1,
                """
                    invoke-static {v$register}, $FIX->nanToZero(F)F
                    move-result v$register
                """.trimIndent(),
            )
        }
    }
}
