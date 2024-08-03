package org.capnproto;

import com.squareup.javapoet.JavaFile;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

public class CapnpcJavaMain {
    public static void main(String[] args) throws IOException {
        if (args.length < 1) return;
        try (final SeekableByteChannel channel = Files.newByteChannel(Paths.get(args[0]))){
            final MessageReader reader = Serialize.read(channel);
            final Schema.CodeGeneratorRequest.Reader request = reader.getRoot(Schema.CodeGeneratorRequest.factory);

            final HashMap<Long, Schema.Node.Reader> nodeMaps = new HashMap<>();

            for (final Schema.Node.Reader node : request.getNodes()) {
                nodeMaps.put(node.getId(), node);
            }

            for (final Schema.CodeGeneratorRequest.RequestedFile.Reader requestedFile : request.getRequestedFiles()) {
                final Schema.Node.Reader node = nodeMaps.get(requestedFile.getId());

                final JavaFile file = Generator.makeJavaFile(node);
                try (final BufferedWriter writer = Files.newBufferedWriter(Paths.get(file.typeSpec.name + ".java"))) {
                    file.writeTo(writer);
                }
            }
        }
    }
}
