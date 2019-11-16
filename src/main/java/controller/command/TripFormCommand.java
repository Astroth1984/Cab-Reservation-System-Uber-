package controller.command;


import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Positive;


@Data
@Accessors(chain = true)
public class TripFormCommand {
	
	 @NotBlank
	    private String sourceStop;

	    @NotBlank
	    private String destinationStop;

	    @NotBlank
	    private String busCode;

	    @Positive
	    private int tripDuration;

	    @Positive
	    private int tripFare;

}
