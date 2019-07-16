package us.myles_selim.cp_ladder;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import discord4j.core.event.EventDispatcher;
import discord4j.core.event.domain.Event;

public interface EventListener {

	public @interface EventSubscriber {}

	/***
	 * @deprecated Call, don't override
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Deprecated
	public default void setup(EventDispatcher dispatch) {
		for (Method m : getClass().getDeclaredMethods()) {
			if (!m.isAnnotationPresent(EventSubscriber.class))
				continue;
			Class<?>[] paramTypes = m.getParameterTypes();
			if (paramTypes.length == 1 && paramTypes[0].isInstance(Event.class)) {
				dispatch.on((Class) paramTypes[0]).subscribe((Object obj) -> {
					try {
						m.invoke(this, obj);
					} catch (IllegalAccessException | IllegalArgumentException
							| InvocationTargetException e) {}
				});
			}
		}
	}

}
