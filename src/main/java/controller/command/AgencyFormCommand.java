package controller.command;


import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@Accessors(chain = true)
public class AgencyFormCommand {
	  @NotBlank
	    @Size(min = 5, max = 100)
	    private String agencyName;

	    @NotBlank
	    @Size(max = 100)
	    private String agencyDetails;

}
