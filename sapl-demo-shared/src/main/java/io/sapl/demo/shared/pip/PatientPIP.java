package io.sapl.demo.shared.pip;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.sapl.api.pip.Attribute;
import io.sapl.api.pip.PolicyInformationPoint;
import io.sapl.demo.domain.Relation;
import io.sapl.demo.repository.RelationRepo;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationContext;

@Slf4j
@PolicyInformationPoint(name="patient", description="retrieves information about patients")
public class PatientPIP {
	

	private Optional<RelationRepo> relationRepo = Optional.empty();
	
	private final ObjectMapper om = new ObjectMapper();
	
	private RelationRepo getRelationRepo(){
		LOGGER.debug("GetRelationRepo...");
		if(!relationRepo.isPresent()){
			LOGGER.debug("RelRepo not present...");
			ApplicationContext context = ApplicationContextProvider.getApplicationContext();
			LOGGER.debug("Context found: {}", context);
			relationRepo = Optional.of(ApplicationContextProvider.getApplicationContext().getBean(RelationRepo.class));
		}
		LOGGER.debug("Found required instance of RelationRepo: {}", relationRepo.isPresent());
		return relationRepo.get();
	}
	
	@Attribute(name="related")
	public JsonNode getRelations (JsonNode value, Map<String, JsonNode> variables) {
		List<String> returnList = new ArrayList<>();
		try{
			int id = Integer.parseInt(value.asText());
			LOGGER.debug("Entering getRelations. ID: {}", id);
			     
			returnList.addAll(getRelationRepo().findByPatientid(id).stream()
                    .map(Relation::getUsername)
                    .collect(Collectors.toList()));			
				
		}catch(NumberFormatException e){
			LOGGER.debug("getRelations couldn't parse the value to Int", e);
		}
		JsonNode result = om.convertValue(returnList, JsonNode.class);
		LOGGER.debug("Result: {}", result);
		return result;
	}
}
