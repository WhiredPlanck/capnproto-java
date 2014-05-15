package org.capnproto;

final class WireHelpers {

    public static int allocate(int ref,
                               SegmentBuilder segment,
                               int amount,
                               byte kind) {
        throw new Error("unimplemented");
    }

    public static ListBuilder initListPointer(int refOffset,
                                              SegmentBuilder segment,
                                              int elementCount,
                                              byte elementSize) {
        throw new Error("unimplemented");
    }

    public static ListBuilder initStructListPointer(int refOffset,
                                                    SegmentBuilder segment,
                                                    int elementCount,
                                                    StructSize elementSize) {
        if (elementSize.preferredListEncoding != FieldSize.INLINE_COMPOSITE) {
            //# Small data-only struct. Allocate a list of primitives instead.
            return initListPointer(refOffset, segment, elementCount,
                                   elementSize.preferredListEncoding);
        }

        int wordsPerElement = elementSize.total();

        throw new Error("unimplemented");
    }

    public static void initTextPointer(int refOffset,
                                       SegmentBuilder segment,
                                       int size) {
        throw new Error("unimplemented");
    }

    public static void setTextPointer(int refOffset,
                                      SegmentBuilder segment,
                                      Text.Reader value) {
        throw new Error("unimplemented");
    }

    public static StructReader readStructPointer(SegmentReader segment,
                                                 int refOffset,
                                                 int nestingLimit) {

        // TODO error handling

        if (nestingLimit < 0) {
            throw new DecodeException("Message is too deeply nested or contains cycles.");
        }

        long ref = WirePointer.get(segment.buffer, refOffset);
        int ptrOffset = WirePointer.target(refOffset, ref);
        int structPtr = WirePointer.structPointer(ref);
        int dataSizeWords = StructPointer.dataSize(structPtr);

        return new StructReader(segment,
                                ptrOffset * 8,
                                (ptrOffset + dataSizeWords),
                                dataSizeWords * 64,
                                StructPointer.ptrCount(structPtr),
                                (byte)0,
                                nestingLimit - 1);

    }


    public static ListReader readListPointer(SegmentReader segment,
                                             int refOffset,
                                             byte expectedElementSize,
                                             int nestingLimit) {

        long ref = WirePointer.get(segment.buffer, refOffset);

        // TODO check for null, follow fars, nestingLimit
        if (WirePointer.isNull(ref)) {
            return new ListReader();
        }

        int listPtr = WirePointer.listPointer(ref);

        int ptrOffset = WirePointer.target(refOffset, ref);
        long ptr = WirePointer.get(segment.buffer, ptrOffset);

        switch (ListPointer.elementSize(listPtr)) {
        case FieldSize.INLINE_COMPOSITE : {
            int wordCount = ListPointer.inlineCompositeWordCount(listPtr);

            long tag = ptr;
            ptrOffset += 1;

            // TODO bounds check

            int size = WirePointer.inlineCompositeListElementCount(tag);

            int structPtr = WirePointer.structPointer(tag);
            int wordsPerElement = StructPointer.wordSize(structPtr);

            // TODO check that elemements do not overrun word count

            // TODO check whether the size is compatible

            return new ListReader(segment,    // TODO follow fars
                                  ptrOffset * 8, //
                                  size,
                                  wordsPerElement * 64,
                                  StructPointer.dataSize(structPtr) * 64,
                                  StructPointer.ptrCount(structPtr),
                                  nestingLimit - 1);
        }
        case FieldSize.VOID : break;
        default :
            throw new Error("unrecognized element size");
        }

        throw new Error();
    }

    public static Text.Reader readTextPointer(SegmentReader segment,
                                              int refOffset) {
        long ref = WirePointer.get(segment.buffer, refOffset);

        if (WirePointer.isNull(ref)) {
            // XXX should use the default value
            return new Text.Reader(java.nio.ByteBuffer.wrap(new byte[0]), 0, 0);
        }

        int ptrOffset = WirePointer.target(refOffset, ref);
        int listPtr = WirePointer.listPointer(ref);
        int size = ListPointer.elementCount(listPtr);

        if (WirePointer.kind(ref) != WirePointer.LIST) {
            throw new DecodeException("Message contains non-list pointer where text was expected.");
        }

        if (ListPointer.elementSize(listPtr) != FieldSize.BYTE) {
            throw new DecodeException("Message contains list pointer of non-bytes where text was expected.");
        }

        // TODO bounds check?

        if (size == 0 || segment.buffer.get(8 * ptrOffset + size - 1) != 0) {
            throw new DecodeException("Message contains text that is not NUL-terminated.");
        }

        return new Text.Reader(segment.buffer, ptrOffset, size - 1);
    }
}
