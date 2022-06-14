package com.redaggr.handel;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;
import static org.objectweb.asm.Opcodes.ACC_NATIVE;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.BIPUSH;
import static org.objectweb.asm.Opcodes.DRETURN;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.DUP2;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.ICONST_2;
import static org.objectweb.asm.Opcodes.ICONST_3;
import static org.objectweb.asm.Opcodes.ICONST_4;
import static org.objectweb.asm.Opcodes.ICONST_5;
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
public class WebServletMethodOnceParameterVisitor extends ClassVisitor {

    private final String className;

    public WebServletMethodOnceParameterVisitor(int api, ClassWriter classVisitor, String className) {
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
                    && "service(Ljavax/servlet/http/HttpServletRequest;Ljavax/servlet/http/HttpServletResponse;)V".equals(name + descriptor)) {
                // 不是构造方法,不是抽象方法,不是原生方法的时候 添加参数打印
                mv = new WebServletMethodOnceParameterAdapter(api, mv, access, name, descriptor, className);
            }
        }
        return mv;
    }

    private static class WebServletMethodOnceParameterAdapter extends MethodVisitor {
        private final int methodAccess;
        private final String methodName;
        private final String methodDesc;
        private final String className;

        public WebServletMethodOnceParameterAdapter(int api, MethodVisitor mv, int methodAccess, String methodName, String methodDesc, String className) {
            super(api, mv);
            this.methodAccess = methodAccess;
            this.methodName = methodName;
            this.methodDesc = methodDesc;
            this.className = className;
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

            Type methodType = Type.getMethodType(methodDesc);
            Type[] argumentTypes = methodType.getArgumentTypes();
            printParam(slotIndex, argumentTypes);
            // 入参可能无法处理基本类型，只支持对象 TODO
            super.visitMethodInsn(INVOKESTATIC, "com/redaggr/util/ParameterUtils", "printValueOnStackInParamByArr", "([Ljava/lang/Object;)V", false);

            setClassInfo(className, methodName, methodDesc);
            // 其次，调用父类的方法实现
            super.visitCode();
        }

        private void printParam(int slotIndex, Type[] argumentTypes) {
            int paramLength = argumentTypes.length;
            if (paramLength > 20 ) {
                // 不处理入参是20个参数的方法
                super.visitCode();
            }
            // 生成一个入参大小为20的数组
            // 超过20个入参则不打印入参数据 TODO
            super.visitIntInsn(BIPUSH, 20);
            super.visitTypeInsn(ANEWARRAY, "java/lang/Object");

            int arrIndex = 0;
            for (Type t : argumentTypes) {
                super.visitInsn(DUP); // 复制一个arr
                int size = t.getSize(); // 如果是占2位置的就不能通过+1解决了
                int opcode = t.getOpcode(ILOAD);
                switch (arrIndex) {
                    case 0:
                        super.visitInsn(ICONST_0);
                        break;
                    case 1:
                        super.visitInsn(ICONST_1);
                        break;
                    case 2:
                        super.visitInsn(ICONST_2);
                        break;
                    case 3:
                        super.visitInsn(ICONST_3);
                        break;
                    case 4:
                        super.visitInsn(ICONST_4);
                        break;
                    case 5:
                        super.visitInsn(ICONST_5);
                        break;
                    default:
                        super.visitIntInsn(BIPUSH, arrIndex);
                        break;
                }
                super.visitVarInsn(opcode, slotIndex);
                super.visitInsn(AASTORE); // 消耗堆栈上的数据存储
                slotIndex += size;
                arrIndex++;
            }
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
                    // HttpServlet 打印入参的response
                    boolean isStatic = ((methodAccess & ACC_STATIC) != 0);
                    int slotIndex = isStatic ? 0 : 1;

                    Type methodType = Type.getMethodType(methodDesc);
                    Type[] argumentTypes = methodType.getArgumentTypes();
                    printParam(slotIndex, argumentTypes);
                    // 入参可能无法处理基本类型，只支持对象 TODO
                    super.visitMethodInsn(INVOKESTATIC, "com/redaggr/util/ParameterUtils", "printValueOnStackOutParamByArr", "([Ljava/lang/Object;)V", false);
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

        private void setClassInfo(String className, String methodName, String methodDesc) {
            super.visitLdcInsn(className.replaceAll("/", ".") + "#" + methodName + methodDesc);
            super.visitMethodInsn(INVOKESTATIC, "com/redaggr/util/ParameterUtils", "setClassInfo", "(Ljava/lang/String;)V", false);
        }
    }
}