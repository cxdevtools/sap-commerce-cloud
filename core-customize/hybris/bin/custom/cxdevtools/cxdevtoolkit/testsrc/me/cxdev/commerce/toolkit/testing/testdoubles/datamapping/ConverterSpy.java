package me.cxdev.commerce.toolkit.testing.testdoubles.datamapping;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.hybris.platform.servicelayer.dto.converter.ConversionException;
import de.hybris.platform.servicelayer.dto.converter.Converter;

public class ConverterSpy<INPUT, OUTPUT> implements Converter<INPUT, OUTPUT> {
	public List<INPUT> observedInputs = new ArrayList<>();
	public List<OUTPUT> observedOutputs = new ArrayList<>();
	public List<OUTPUT> deliveredOutputs = new ArrayList<>();
	public List<OUTPUT> outputs = new ArrayList<>();

	private int observedInputsCounter = 0;
	private int observedOutputsCounter = 0;
	private int deliveredOutputsCounter = 0;
	private int outputsCounter = 0;

	public static <INPUT, OUTPUT> ConverterSpy<INPUT, OUTPUT> forResult(OUTPUT output) {
		return new ConverterSpy<>(output);
	}

	public ConverterSpy(OUTPUT output) {
		this.outputs.add(output);
	}

	public ConverterSpy(OUTPUT output, OUTPUT... outputs) {
		this.outputs.add(output);
		this.outputs.addAll(Arrays.asList(outputs));
	}

	@Override
	public OUTPUT convert(INPUT input) {
		this.observedInputs.add(input);
		return getNextOutput();
	}

	@Override
	public OUTPUT convert(INPUT input, OUTPUT output) throws ConversionException {
		this.observedInputs.add(input);
		this.observedOutputs.add(output);
		return getNextOutput();
	}

	public INPUT observedInput() {
		if (observedInputsCounter >= observedInputs.size()) {
			observedInputsCounter = 0;
		}
		return observedInputs.get(observedInputsCounter++);
	}

	public OUTPUT observedOutput() {
		if (observedOutputsCounter >= observedOutputs.size()) {
			observedOutputsCounter = 0;
		}
		return observedOutputs.get(observedOutputsCounter++);
	}

	public OUTPUT output() {
		if (deliveredOutputsCounter >= deliveredOutputs.size()) {
			deliveredOutputsCounter = 0;
		}
		return deliveredOutputs.get(deliveredOutputsCounter++);
	}

	private OUTPUT getNextOutput() {
		if (++outputsCounter == this.outputs.size()) {
			outputsCounter = 0;
		}

		OUTPUT output = this.outputs.get(outputsCounter);
		deliveredOutputs.add(output);
		return output;
	}
}
