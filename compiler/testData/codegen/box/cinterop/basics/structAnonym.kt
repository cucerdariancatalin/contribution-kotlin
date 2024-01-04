// TARGET_BACKEND: NATIVE
// MODULE: cinterop
// FILE: structAnonym.def
---

/*
 *  Test of return/send-by-value for aggregate type (struct or union) with anonymous inner struct or union member.
 *  Specific issues: alignment, packed, nested named and anon struct/union, other anon types (named field  of anon struct type; anon bitfield)
 */

#include <inttypes.h>

#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Winitializer-overrides"

union _GLKVector3
{
    struct { float x, y, z; };
    struct { float r, g, b; };
    struct { float s, t, p; };
    float v[3];
};

static union _GLKVector3 get_GLKVector3() {
    union _GLKVector3 ret = {{1, 2, 3}};
    return ret;
}

static float hash_GLKVector3(union _GLKVector3 x) {
    union _GLKVector3 ret = {{1, 2, 3}};
    return x.x + 2.0f * x.y + 4.0f * x.z;
}

// trivial alignment: member is already aligned, but this implies implicit larger alignment of the root struct
struct StructAnonRecordMember_ImplicitAlignment {
    int32_t a[4];
    struct {
        int b  __attribute__((aligned(16)));
    };
};

static struct StructAnonRecordMember_ImplicitAlignment retByValue_StructAnonRecordMember_ImplicitAlignment() {
    struct StructAnonRecordMember_ImplicitAlignment t = {
        .a = {1,2,3,4},
        .b = 42
    };
    return t;
}

struct StructAnonRecordMember_ExplicitAlignment {
    char a;
    struct {
        __attribute__((aligned(4)))
        char x;
    };
};

static struct StructAnonRecordMember_ExplicitAlignment retByValue_StructAnonRecordMember_ExplicitAlignment() {
    struct StructAnonRecordMember_ExplicitAlignment t = {
        .a = 'a',
        .x = 'x'
    };
    return t;
}

// Deep nesting
struct StructAnonRecordMember_Nested {
    int x;
    union { // implicitly aligned to 8 bytes due to int64, or 4 bytes at 32-bit arch
        int a[2];
        struct {
            int64_t b;
        };
    };
    char z;
    double y;
};

static struct StructAnonRecordMember_Nested retByValue_StructAnonRecordMember_Nested() {
    struct StructAnonRecordMember_Nested c = {
        .x = 37,
        .b = 42,
        .z = 'z',
        .y = 3.14
    };
    return c;
}

static int sendByValue_StructAnonRecordMember_Nested(struct StructAnonRecordMember_Nested c) {
    return c.a[0] + 2 * c.a[1];
}

// Basic, 2 levels

struct StructAnonRecordMember_Complicate {
    char first; // __attribute__((aligned(16)));
    union {
        int a[2];
        union { char c1; int c2; };
        struct { char b1; int64_t b2; };  // implicit 64-bits alignment
    };
    char second __attribute__((aligned(16)));
    struct {
        char x;
        struct { int64_t b11, b12; } Y2;
        int32_t f  __attribute__((aligned(16)));
    }; // __attribute__((aligned(16)));
    char last;
};

#define INIT(T, x) 	struct T x = \
{ \
    .first = 'a', \
    .b1 = 'b', \
    .b2 = 42, \
    .second = 's', \
    .last = 'z', \
    .f = 314, \
    .Y2 = {11, 12} \
}

static struct StructAnonRecordMember_Complicate retByValue_StructAnonRecordMember_Complicate() {
    INIT(StructAnonRecordMember_Complicate, c);
    return c;
}

struct StructAnonRecordMember_Packed {
    char first;
    union {
        int a[2];
        union { char c1; int c2; };
        struct { char b1; int64_t b2; };
    };
    char second;
    struct {
        char x;
        struct { int64_t b11, b12; } Y2;
        int32_t f;
    } __attribute__((aligned(16)));
    char last;
} __attribute__ ((packed));

static struct StructAnonRecordMember_Packed retByValue_StructAnonRecordMember_Packed() {
    INIT(StructAnonRecordMember_Packed, c);
    return c;
}

// Nested struct may be packed too
#pragma pack(1)
struct StructAnonRecordMember_PragmaPacked {
    char first;
    union {
        int a[2];
        union { char c1; int c2; };
        struct { char b1; int64_t b2; };
    };
    char second;
    struct {
        char x;
        struct { int64_t b11, b12; } Y2;
        int32_t f  __attribute__((aligned(16))); // another kind of alignment
    };
    char last;
} __attribute__ ((packed));
#pragma pack()

static struct StructAnonRecordMember_PragmaPacked retByValue_StructAnonRecordMember_PragmaPacked() {
    INIT(StructAnonRecordMember_PragmaPacked, c);
    return c;
}

#pragma pack(2)
struct StructAnonRecordMember_Packed2 {
    char first;
    union {
        int a[2];
        union { char c1; int c2; };
        struct { char b1; int64_t b2; };
    };
    char second;
    struct {
        char x;
        struct { int64_t b11, b12; } Y2;
        int32_t f;
    } __attribute__((aligned(16)));
    char last;
} __attribute__ ((packed));
#pragma pack()

static struct StructAnonRecordMember_Packed2 retByValue_StructAnonRecordMember_Packed2() {
    INIT(StructAnonRecordMember_Packed2, c);
    return c;
}

#pragma clang diagnostic pop

// MODULE: main(cinterop)
// FILE: main.kt

@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)


import kotlinx.cinterop.*
import kotlin.test.*
import structAnonym.*


fun test_GLKVector3() {
    get_GLKVector3().useContents {
        assertEquals(1.0f, x)
        assertEquals(2.0f, g)
        assertEquals(3.0f, p)
        r = 0.1f
        g = 0.2f
        b = 0.3f
        assertEquals(v[0], r)
        assertEquals(v[1], g)
        assertEquals(v[2], b)

        val ret = hash_GLKVector3(this.readValue())
        assertEquals(s + 2f * t + 4f * p , ret)
    }
}

fun test_StructAnonRecordMember_ImplicitAlignment() {
    retByValue_StructAnonRecordMember_ImplicitAlignment()
            .useContents {
                assertEquals(1, a[0])
                assertEquals(4, a[3])
                assertEquals(42, b)
            }
}

fun test_StructAnonRecordMember_ExplicitAlignment() {
    retByValue_StructAnonRecordMember_ExplicitAlignment()
            .useContents {
                assertEquals('a', a.toInt().toChar())
                assertEquals('x', x.toInt().toChar())
            }
}

fun test_StructAnonRecordMember_Nested() {
    retByValue_StructAnonRecordMember_Nested()
            .useContents {
                assertEquals(37, x)
                assertEquals(42, b)
                assertEquals('z', z.toInt().toChar())
                assertEquals(3.14, y)

                a[0] = 3
                a[1] = 5
                assertEquals(3 + 2*5, sendByValue_StructAnonRecordMember_Nested(this.readValue()))
            }
}

fun test_StructAnonym_Complicate() {
    retByValue_StructAnonRecordMember_Complicate()
            .useContents{
                assertEquals('a', first.toInt().toChar())
                assertEquals('s', second.toInt().toChar())
                assertEquals('z', last.toInt().toChar())
                assertEquals('b', b1.toInt().toChar())
                assertEquals(42L, b2)
                assertEquals(314, f)
                assertEquals(11L, Y2.b11)
            }
}

fun test_StructAnonym_Packed() {
    retByValue_StructAnonRecordMember_Packed2()
            .useContents{
                assertEquals('a', first.toInt().toChar())
                assertEquals('s', second.toInt().toChar())
                assertEquals('z', last.toInt().toChar())
                assertEquals('b', b1.toInt().toChar())
                assertEquals(42L, b2)
                assertEquals(314, f)
                assertEquals(11L, Y2.b11)
            }
}

fun test_StructAnonym_PragmaPacked() {
    retByValue_StructAnonRecordMember_PragmaPacked()
            .useContents{
                assertEquals('a', first.toInt().toChar())
                assertEquals('s', second.toInt().toChar())
                assertEquals('z', last.toInt().toChar())
                assertEquals('b', b1.toInt().toChar())
                assertEquals(42L, b2)
                assertEquals(314, f)
                assertEquals(11L, Y2.b11)
            }
}

fun test_StructAnonym_Packed2() {
    retByValue_StructAnonRecordMember_Packed2()
            .useContents{
                assertEquals('a', first.toInt().toChar())
                assertEquals('s', second.toInt().toChar())
                assertEquals('z', last.toInt().toChar())
                assertEquals('b', b1.toInt().toChar())
                assertEquals(42L, b2)
                assertEquals(314, f)
                assertEquals(11L, Y2.b11)
            }
}

fun box(): String {
    test_GLKVector3()
    test_StructAnonRecordMember_ImplicitAlignment()
    test_StructAnonRecordMember_ExplicitAlignment()
    test_StructAnonRecordMember_Nested()
    test_StructAnonym_Complicate()
    test_StructAnonym_Packed()
    test_StructAnonym_PragmaPacked()
    test_StructAnonym_Packed2()

    return "OK"
}
