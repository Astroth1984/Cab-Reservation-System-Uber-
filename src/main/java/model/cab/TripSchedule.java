package model.cab;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
@Document(collection = "tripschedule")
public class TripSchedule {
    @Id
    private String id;

    @DBRef
    private Trip tripDetail;

    @DBRef(lazy = true)
    private List<Ticket> ticketsSold;

    private String tripDate;

    private int availableSeats;
}
