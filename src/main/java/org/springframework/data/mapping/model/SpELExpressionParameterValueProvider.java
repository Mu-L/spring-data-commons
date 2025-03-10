/*
 * Copyright 2012-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.mapping.model;

import org.springframework.core.convert.ConversionService;
import org.springframework.data.mapping.Parameter;
import org.springframework.data.mapping.PersistentProperty;
import org.springframework.lang.Nullable;

/**
 * {@link ParameterValueProvider} that can be used to front a {@link ParameterValueProvider} delegate to prefer a SpEL
 * expression evaluation over directly resolving the parameter value with the delegate.
 *
 * @author Oliver Gierke
 * @author Mark Paluch
 */
public class SpELExpressionParameterValueProvider<P extends PersistentProperty<P>>
		implements ParameterValueProvider<P> {

	private final SpELExpressionEvaluator evaluator;
	private final ConversionService conversionService;
	private final ParameterValueProvider<P> delegate;

	public SpELExpressionParameterValueProvider(SpELExpressionEvaluator evaluator, ConversionService conversionService,
			ParameterValueProvider<P> delegate) {

		this.evaluator = evaluator;
		this.conversionService = conversionService;
		this.delegate = delegate;
	}

	@Nullable
	public <T> T getParameterValue(Parameter<T, P> parameter) {

		if (!parameter.hasSpelExpression()) {
			return delegate == null ? null : delegate.getParameterValue(parameter);
		}

		Object object = evaluator.evaluate(parameter.getSpelExpression());
		return object == null ? null : potentiallyConvertSpelValue(object, parameter);
	}

	/**
	 * Hook to allow to massage the value resulting from the Spel expression evaluation. Default implementation will
	 * leverage the configured {@link ConversionService} to massage the value into the parameter type.
	 *
	 * @param object the value to massage, will never be {@literal null}.
	 * @param parameter the {@link Parameter} we create the value for
	 * @return
	 */
	@Nullable
	protected <T> T potentiallyConvertSpelValue(Object object, Parameter<T, P> parameter) {
		return conversionService.convert(object, parameter.getRawType());
	}
}
