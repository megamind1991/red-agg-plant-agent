package com.redaggr.handel;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static org.objectweb.asm.Opcodes.ACC_NATIVE;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.DRETURN;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.DUP2;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.IRETURN;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.RETURN;


/**
 * @author : 0006841 油面筋
 * @Description : 为类的方法增加参数打印 比较好 利用调用静态方法处理<br>
 * @Problem : 异常的返回不正常
 * @taskId <br>
 * @return : null
 */
@Deprecated
public class WebServletMethodParameterVisitor extends ClassVisitor {

    public WebServletMethodParameterVisitor(int api, ClassVisitor classVisitor) {
        super(api, classVisitor);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
        MethodVisitor mv = super.visitMethod(access, name, descriptor, signature, exceptions);
        if (mv != null && !name.equals("<init>")) {
            boolean isAbstractMethod = (access & ACC_ABSTRACT) != 0;
            boolean isNativeMethod = (access & ACC_NATIVE) != 0;
            if (!isAbstractMethod
                    && !isNativeMethod
                    && "service(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V".equals(name + descriptor)) {
                // 不是构造方法,不是抽象方法,不是原生方法的时候 添加参数打印
                mv = new WebServletMethodParameterAdapter(api, mv, access, name, descriptor);
            }
        }
        return mv;
    }

    private static class WebServletMethodParameterAdapter extends MethodVisitor {
        private final int methodAccess;
        private final String methodName;
        private final String methodDesc;

        public WebServletMethodParameterAdapter(int api, MethodVisitor mv, int methodAccess, String methodName, String methodDesc) {
            super(api, mv);
            this.methodAccess = methodAccess;
            this.methodName = methodName;
            this.methodDesc = methodDesc;
        }

        @Override
        public void visitCode() {
            // 把HttpServletResponse 转化为 HttpServletResponseWrapper以便于后续回去返回中的数据
            super.visitTypeInsn(NEW, "com/redaggr/servletAgent/ServletResponseProxy");
            super.visitInsn(DUP);
            super.visitVarInsn(ALOAD, 2);
            super.visitMethodInsn(INVOKESPECIAL, "com/redaggr/servletAgent/ServletResponseProxy", "<init>", "(Ljavax/servlet/http/HttpServletResponse;)V", false);
            super.visitVarInsn(ASTORE, 2);

            // 首先，处理自己的代码逻辑
            boolean isStatic = ((methodAccess & ACC_STATIC) != 0);
            int slotIndex = isStatic ? 0 : 1;

            printMessage("Method Enter: " + methodName + methodDesc);

            Type methodType = Type.getMethodType(methodDesc);
            Type[] argumentTypes = methodType.getArgumentTypes();
            for (Type t : argumentTypes) {
                int sort = t.getSort();
                int size = t.getSize();
                String descriptor = t.getDescriptor();
                int opcode = t.getOpcode(ILOAD);
                super.visitVarInsn(opcode, slotIndex);
                if (sort >= Type.BOOLEAN && sort <= Type.DOUBLE) {
                    String methodDesc = String.format("(%s)V", descriptor);
                    printValueOnStack(methodDesc);
                } else {
                    printValueOnStack("(Ljava/lang/Object;)V");
                }

                slotIndex += size;
            }

            // 其次，调用父类的方法实现
            super.visitCode();
        }

        @Override
        public void visitInsn(int opcode) {
            // Servlet的返回应该打印的是入参的response
            if ((opcode >= IRETURN && opcode <= RETURN) || opcode == ATHROW) {
                printMessage("Method Exit: " + methodName + methodDesc);
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
                    // HttpServlet 打印入参的response
                    boolean isStatic = ((methodAccess & ACC_STATIC) != 0);
                    int slotIndex = isStatic ? 0 : 1;

                    Type methodType = Type.getMethodType(methodDesc);
                    Type[] argumentTypes = methodType.getArgumentTypes();
                    for (Type t : argumentTypes) {
                        int sort = t.getSort();
                        int size = t.getSize();
                        String descriptor = t.getDescriptor();
                        int returnParamOpcode = t.getOpcode(ILOAD);
                        super.visitVarInsn(returnParamOpcode, slotIndex);
                        if (sort >= Type.BOOLEAN && sort <= Type.DOUBLE) {
                            String methodDesc = String.format("(%s)V", descriptor);
                            printValueOnStack(methodDesc);
                        } else {
                            printValueOnStack("(Ljava/lang/Object;)V");
                        }

                        slotIndex += size;
                    }
                } else {
                    printMessage("    abnormal return");
                }
            }

            // 其次，调用父类的方法实现
            super.visitInsn(opcode);
        }

        private void printMessage(String str) {
            super.visitLdcInsn(str);
            super.visitMethodInsn(INVOKESTATIC, "com/redaggr/util/ParameterUtils", "printText", "(Ljava/lang/String;)V", false);
        }

        private void printValueOnStack(String descriptor) {
            super.visitMethodInsn(INVOKESTATIC, "com/redaggr/util/ParameterUtils", "printValueOnStack", descriptor, false);
        }
    }
}