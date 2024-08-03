package org.capnproto;

import org.capnproto.utils.StrUtil;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class GeneratorContext {
    public final Schema.CodeGeneratorRequest.Reader request;
    public final HashMap<Long, Schema.Node.Reader> nodeMap;
    public final HashMap<Long, List<String>> scopeMap;
    public final HashMap<Long, Long> nodeParents;
    public final String capnpRoot;


    public GeneratorContext(final Schema.CodeGeneratorRequest.Reader request,
                            final HashMap<Long, Schema.Node.Reader> nodeMap,
                            final HashMap<Long, List<String>> scopeMap,
                            final HashMap<Long, Long> nodeParents,
                            final String capnpRoot) {
        this.request = request;
        this.nodeMap = nodeMap;
        this.scopeMap = scopeMap;
        this.nodeParents = nodeParents;
        this.capnpRoot = capnpRoot;
    }

    private Optional<String> annotationText(final Schema.Node.Reader node, final long annotationId) {
        Objects.requireNonNull(node);
        for (final Schema.Annotation.Reader annotation : node.getAnnotations()) {
            if (annotation.getId() == annotationId) {
                return Optional.of(annotation.getValue().getText().toString());
            }
        }
        return Optional.empty();
    }

    public Optional<String> nodePackageName(final long nodeId) {
        final Schema.Node.Reader node = nodeMap.get(nodeId);
        return annotationText(node, Constants.PACKAGE_ANNOTATION_ID);
    }

    public Optional<String> nodeOuterClassname(final long nodeId) {
        final Schema.Node.Reader node = nodeMap.get(nodeId);
        return annotationText(node, Constants.OUTER_CLASSNAME_ANNOTATION_ID);
    }

    public static Optional<GeneratorContext> newFromMessage(final MessageReader message) {
        Objects.requireNonNull(message);
        final GeneratorContext ctx = new GeneratorContext(
                message.getRoot(Schema.CodeGeneratorRequest.factory),
                new HashMap<>(),
                new HashMap<>(),
                new HashMap<>(),
                ""
        );

        for (final Schema.Node.Reader node : ctx.request.getNodes()) {
            ctx.nodeMap.put(node.getId(), node);
            ctx.nodeParents.put(node.getId(), node.getScopeId());
        }

        for (final Schema.Node.Reader node : ctx.request.getNodes()) {
            if (node.isInterface()) {
                final Schema.Node.Interface.Reader interf = node.getInterface();
                for (final Schema.Method.Reader method : interf.getMethods()) {
                    final long paramStructType = method.getParamStructType();
                    if (ctx.nodeParents.get(paramStructType) == 0) {
                        ctx.nodeParents.put(paramStructType, node.getId());
                    }
                    final long resultStructType = method.getResultStructType();
                    if (ctx.nodeParents.get(resultStructType) == 0) {
                        ctx.nodeParents.put(resultStructType, node.getId());
                    }
                }
            }
        }

        for (final Schema.CodeGeneratorRequest.RequestedFile.Reader requestedFile : ctx.request.getRequestedFiles()) {
            final long id = requestedFile.getId();

            for (final Schema.CodeGeneratorRequest.RequestedFile.Import.Reader imp : requestedFile.getImports()) {
                final Path path = Paths.get(imp.getName().toString());
                final String rootName = StrUtil.subBeforeLast(path.getFileName().toString(), ".");
                ctx.populateScopeMap(List.of(rootName), imp.getId());
            }

            final String packageName = ctx.nodePackageName(id).get();
            final String outerClassname = ctx.nodeOuterClassname(id).get();
            final String root = String.format("%s.%s", packageName, outerClassname);
            ctx.populateScopeMap(List.of(root), id);
        }

        return Optional.of(ctx);
    }

    private void populateScopeMap(final List<String> scopeNames, final long nodeId) {
        scopeMap.put(nodeId, scopeNames);

        final Schema.Node.Reader node = nodeMap.get(nodeId);
        if (node == null) return;

        for (final Schema.Node.NestedNode.Reader nestedNode : node.getNestedNodes()) {
            final ArrayList<String> nScopeNames = new ArrayList<>(scopeNames);
            final long nestedNodeId = nestedNode.getId();
            final Schema.Node.Reader nNode = nodeMap.get(nestedNodeId);
            if (nNode != null) {
                if (nNode.isEnum()) {
                    nScopeNames.add(nestedNode.getName().toString());
                    populateScopeMap(nScopeNames, nestedNodeId);
                } else {
                    final ArrayList<String> _scopeNames = new ArrayList<>(scopeNames);
                    _scopeNames.add(nestedNode.getName().toString());
                    populateScopeMap(_scopeNames, nestedNodeId);
                }
            }
        }

        if (node.isStruct()) {
            final Schema.Node.Struct.Reader struct = node.getStruct();
            for (final Schema.Field.Reader field : struct.getFields()) {
                if (field.isGroup()) {
                    final Schema.Field.Group.Reader group = field.getGroup();
                    final ArrayList<String> _scopeNames = new ArrayList<>(scopeNames);
                    _scopeNames.add(StrUtil.capitalize(field.getName().toString()));
                    populateScopeMap(_scopeNames, group.getTypeId());
                }
            }
        }

    }
}
