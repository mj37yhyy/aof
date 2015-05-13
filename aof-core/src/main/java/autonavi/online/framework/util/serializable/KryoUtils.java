package autonavi.online.framework.util.serializable;

import org.objenesis.strategy.InstantiatorStrategy;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.Registration;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

public class KryoUtils {
	private Kryo kryo = new Kryo();

	public KryoUtils() {
	}

	/**
	 * 
	 * @param references
	 *            是否支持循环引用。默认为true。
	 * @param registrationRequired
	 *            如果为true，在遇到未注册类时则抛出一个异常。默认为false。
	 * @param strategy
	 *            定义了一个策略来确定类的最佳创建器。如果传入StdInstantiatorStrategy，则不调用任何构造函数创建对
	 *            。如果传入SerializingInstantiatorStrategy则模仿Java内置的序列化
	 */
	public KryoUtils(boolean references, boolean registrationRequired,
			InstantiatorStrategy strategy) {
		kryo.setReferences(references);
		kryo.setRegistrationRequired(registrationRequired);
		/*
		 * 设置使用的 newInstantiator(Class)
		 * 策略创建对象。如果传入StdInstantiatorStrategy，则不调用任何构造函数创建对
		 * 。如果传入SerializingInstantiatorStrategy则模仿Java内置的序列化
		 */
		kryo.setInstantiatorStrategy(strategy);
	}

	/**
	 * 序列化
	 * 
	 * @param bean
	 * @return
	 */
	public byte[] serialize(Object bean) {
		Output output = null;
		// ByteArrayOutputStream outStream = new ByteArrayOutputStream();
		// output = new Output( outStream , 4096);
		output = new Output(1, 4096);
		kryo.writeObject(output, bean);
		byte[] b = output.toBytes();
		return b;

	}

	/**
	 * 反序列化
	 * 
	 * @param b
	 * @param tClass
	 * @return
	 */
	public <T> T deserialize(byte[] b, Class<T> tClass) {
		Registration registration = kryo.register(tClass);
		Input input = null;
		// input = new Input(new
		// ByteArrayInputStream(outStream.toByteArray()),4096);
		input = new Input(b);
		@SuppressWarnings("unchecked")
		T t = (T) kryo.readObject(input, registration.getType());
		input.close();
		return t;
	}

}
