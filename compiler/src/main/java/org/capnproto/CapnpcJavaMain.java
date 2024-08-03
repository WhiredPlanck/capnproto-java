package org.capnproto;

import com.squareup.javapoet.JavaFile;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;

public class CapnpcJavaMain {
    public static void main(String[] args) throws IOException {
        if (args.length < 1) return;
        try (final SeekableByteChannel channel = Files.newByteChannel(Paths.get(args[0]))){
            final MessageReader reader = Serialize.read(channel);
            final Schema.CodeGeneratorRequest.Reader request = reader.getRoot(Schema.CodeGeneratorRequest.factory);

            final GeneratorContext ctx = GeneratorContext.newFromMessage(reader).get();

            for (final Schema.CodeGeneratorRequest.RequestedFile.Reader requestedFile : request.getRequestedFiles()) {
                final long id = requestedFile.getId();

                final JavaFile file = Generator.makeJavaFile(ctx, id);
                try (final BufferedWriter writer = Files.newBufferedWriter(Paths.get(file.typeSpec.name + ".java"))) {
                    file.writeTo(writer);
                }
            }
        }
    }
}
