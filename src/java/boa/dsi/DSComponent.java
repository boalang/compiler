package boa.dsi;

import java.util.List;

import com.aol.cyclops.data.async.Queue;
import com.google.protobuf.GeneratedMessage;

public interface DSComponent {

	List<GeneratedMessage> getData();

	boolean getDataInQueue(Queue<GeneratedMessage> queue);

}
