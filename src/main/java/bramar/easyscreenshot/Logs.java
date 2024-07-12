package bramar.easyscreenshot;

import java.io.IOException;
import java.io.PrintWriter;

public class Logs {
	private StringBuilder string = new StringBuilder();
	public void print(Object obj) {
		if(obj instanceof Throwable) printThrowable((Throwable) obj);
		else try {
			String inString = String.valueOf(obj);
			System.out.println(inString);
			string.append(inString).append('\n').append('\n');
			PrintWriter writer = new PrintWriter("logs.log");
			writer.write(string.toString());
			writer.close();
		}catch(Exception ignored) {}
	}
	public void printThrowable(Throwable t) {
		t.printStackTrace();
		String indent = "	";
		Throwable current = t;
		StringBuilder str = new StringBuilder("Exception in thread \"" + Thread.currentThread().getName() + "\" " + t.getClass().getName());
		while((current = current.getCause()) != null) {
			str.append(": " + current.getClass().getName());
		}
		for(StackTraceElement element : t.getStackTrace()) {
			str.append('\n' + indent + "at " + element.getFileName() + "." + element.getMethodName() + "(" + element.getClassName() + ".java:" + element.getLineNumber() + ")");
		}
		current = t;
		while((current = current.getCause()) != null) {
			str.append('\n').append("Caused by: ").append(current.getClass().getName());
			for(StackTraceElement element : current.getStackTrace()) {
				str.append('\n')
				.append(indent)
				.append("at ")
				.append(element.getFileName())
				.append(".")
				.append(element.getMethodName())
				.append("(")
				.append(!element.isNativeMethod() ? element.getClassName() : "Native")
				.append(!element.isNativeMethod() ? ".java:" : " ")
				.append(!element.isNativeMethod() ? element.getLineNumber() : "Method")
				.append(")");
			}
		}
		System.err.println(str);
		string.append(str).append('\n').append('\n');
		PrintWriter writer;
		try {
			writer = new PrintWriter("logs.log");
			writer.write(string.toString());
			writer.close();
		}catch(IOException e) {
			print(e);
		}
		
	}
}
