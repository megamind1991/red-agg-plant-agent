package com.redaggr.handel;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static org.objectweb.asm.Opcodes.ACC_NATIVE;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.DRETURN;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.DUP2;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.RETURN;


/**
 * @author : 0006841 油面筋
 * @Description : 为类的方法增加参数打印 比较好 利用调用静态方法处理<br>
 * @Problem : 异常的返回不正常
 * @taskId <br>
 * @return : null
 */
public class MethodXxlJobParameterVisitor extends ClassVisitor {

    private final String className;

    public MethodXxlJobParameterVisitor(int api, ClassWriter classVisitor, String className) {
        super(api, classVisitor);
        this.className = className;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        if (mv != null && !name.equals("<init>")) {
            boolean isAbstractMethod = (access & ACC_ABSTRACT) != 0;
            boolean isNativeMethod = (access & ACC_NATIVE) != 0;
            if (!isAbstractMethod
                    && !isNativeMethod
                    && "execute()V".equals(name + descriptor)) {
                // 不是构造方法,不是抽象方法,不是原生方法的时候 添加参数打印
                mv = new MethodXxlJobParameterAdapter(api, mv, access, name, descriptor, className);
            }
        }
        return mv;
    }

    private static class MethodXxlJobParameterAdapter extends MethodVisitor {
        private final int methodAccess;
        private final String methodName;
        private final String methodDesc;
        private final String className;

        public MethodXxlJobParameterAdapter(int api, MethodVisitor mv, int methodAccess, String methodName, String methodDesc, String className) {
            super(api, mv);
            this.methodAccess = methodAccess;
            this.methodName = methodName;
            this.methodDesc = methodDesc;
            this.className = className;
        }

        @Override
        public void visitCode() {

            super.visitInsn(ICONST_1);
            super.visitTypeInsn(ANEWARRAY, "java/lang/Object");
            super.visitInsn(DUP);
            super.visitInsn(ICONST_0);
            super.visitVarInsn(ALOAD, 0);
            super.visitFieldInsn(GETFIELD, "com/xxl/job/core/handler/impl/MethodJobHandler", "method", "Ljava/lang/reflect/Method;");
            super.visitInsn(AASTORE);
            super.visitMethodInsn(INVOKESTATIC, "com/redaggr/util/ParameterUtils", "printValueOnStackInParamByArr", "([Ljava/lang/Object;)V", false);

            // 其次，调用父类的方法实现
            super.visitCode();
        }

        @Override
        public void visitInsn(int opcode) {
            // Servlet的返回应该打印的是入参的response
            if ((opcode >= IRETURN && opcode <= RETURN) || opcode == ATHROW) {
                if (opcode >= IRETURN && opcode <= DRETURN) {
                    Type methodType = Type.getMethodType(methodDesc);
                    Type returnType = methodType.getReturnType();
                    int size = returnType.getSize();
                    String descriptor = returnType.getDescriptor();

                    // 判断返回值的占位,F/D是占2位 的所以存在DUP2的情况
                    if (size == 1) {
                        super.visitInsn(DUP);
                    } else {
                        super.visitInsn(DUP2);
                    }
                    String methodDesc = String.format("(%s)V", descriptor);
                    printValueOnStack(methodDesc);
                } else if (opcode == ARETURN) {
                    super.visitInsn(DUP);
                    printValueOnStack("(Ljava/lang/Object;)V");
                } else if (opcode == RETURN) {
                    super.visitLdcInsn("void");
                    printValueOnStack("(Ljava/lang/Object;)V");
                } else {
                    printMessage("    abnormal return");
                }

                // 在这边清除session
                super.visitMethodInsn(INVOKESTATIC, "com/redaggr/util/ParameterUtils", "cleanSession", "()V", false);
            }

            // 其次，调用父类的方法实现
            super.visitInsn(opcode);
        }

        private void printMessage(String str) {
            super.visitLdcInsn(str);
            super.visitMethodInsn(INVOKESTATIC, "com/redaggr/util/ParameterUtils", "printText", "(Ljava/lang/String;)V", false);
        }

        private void printValueOnStack(String descriptor) {
            super.visitMethodInsn(INVOKESTATIC, "com/redaggr/util/ParameterUtils", "printValueOnStack4Return", descriptor, false);
        }
    }
}