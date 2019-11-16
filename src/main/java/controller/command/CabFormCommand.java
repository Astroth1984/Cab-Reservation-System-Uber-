package controller.command;


import lombok.Data;
import lombok.experimental.Accessors;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;


@Data
@Accessors(chain = true)
public class CabFormCommand {
	@NotBlank
    @Size(min = 4, max = 8)
    private String code;

    @Min(value = 10, message = "Cannot enroll a cab with capacity smaller than 10")
    private int capacity;

    @NotBlank
    private String make;

}
