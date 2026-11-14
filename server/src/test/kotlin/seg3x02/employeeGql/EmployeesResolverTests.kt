package seg3x02.employeeGql

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.graphql.GraphQlTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.graphql.test.tester.GraphQlTester
import seg3x02.employeeGql.entity.Employee
import seg3x02.employeeGql.repository.EmployeesRepository
import seg3x02.employeeGql.resolvers.EmployeesResolver

@GraphQlTest(EmployeesResolver::class)
class EmployeesResolverTests {

    @Autowired
    lateinit var graphQlTester: GraphQlTester

    @MockBean
    lateinit var employeesRepository: EmployeesRepository

    private fun employee(id: String, name: String, city: String, salary: Float) =
        Employee(name, "1990-05-14", city, salary, "F", "$name@example.com".lowercase()).also { it.id = id }

    @Test
    fun `query employees returns every stored employee`() {
        given(employeesRepository.findAll()).willReturn(
            listOf(employee("1", "Alice", "Ottawa", 65000f), employee("2", "Bob", "Gatineau", 58000f))
        )

        graphQlTester.document("{ employees { id name city salary email } }")
            .execute()
            .path("employees").entityList(Map::class.java).hasSize(2)
            .path("employees[0].name").entity(String::class.java).isEqualTo("Alice")
            .path("employees[1].city").entity(String::class.java).isEqualTo("Gatineau")
            .path("employees[0].salary").entity(Double::class.java).isEqualTo(65000.0)
    }

    @Test
    fun `query employees returns an empty list when the database is empty`() {
        given(employeesRepository.findAll()).willReturn(emptyList())

        graphQlTester.document("{ employees { id name } }")
            .execute()
            .path("employees").entityList(Map::class.java).hasSize(0)
    }

    @Test
    fun `mutation newEmployee saves the employee with a generated id`() {
        given(employeesRepository.save(any(Employee::class.java))).willAnswer { it.getArgument(0) }

        val mutation = """
            mutation {
              newEmployee(createEmployeeInput: {
                name: "Carla", dateOfBirth: "1995-02-03", city: "Montréal",
                salary: 72000.5, gender: "F", email: "carla@example.com"
              }) { id name dateOfBirth city salary gender email }
            }
        """.trimIndent()

        graphQlTester.document(mutation)
            .execute()
            .path("newEmployee.id").entity(String::class.java).satisfies { assertThat(it).isNotBlank() }
            .path("newEmployee.name").entity(String::class.java).isEqualTo("Carla")
            .path("newEmployee.city").entity(String::class.java).isEqualTo("Montréal")
            .path("newEmployee.salary").entity(Double::class.java).isEqualTo(72000.5)
            .path("newEmployee.email").entity(String::class.java).isEqualTo("carla@example.com")

        val captor = ArgumentCaptor.forClass(Employee::class.java)
        verify(employeesRepository).save(captor.capture())
        assertThat(captor.value.name).isEqualTo("Carla")
        assertThat(captor.value.id).isNotBlank()
    }

    @Test
    fun `mutation newEmployee accepts missing optional fields`() {
        given(employeesRepository.save(any(Employee::class.java))).willAnswer { it.getArgument(0) }

        graphQlTester.document(
            """mutation { newEmployee(createEmployeeInput: {
                 name: "Dan", dateOfBirth: "1988-11-30", city: "Toronto", salary: 50000
               }) { name gender email } }"""
        )
            .execute()
            .path("newEmployee.name").entity(String::class.java).isEqualTo("Dan")
            .path("newEmployee.gender").valueIsNull()
            .path("newEmployee.email").valueIsNull()
    }

    @Test
    fun `mutation newEmployee is rejected when a required field is missing`() {
        graphQlTester.document(
            """mutation { newEmployee(createEmployeeInput: {
                 name: "Eve", city: "Ottawa", salary: 1000
               }) { id } }"""
        )
            .execute()
            .errors()
            .expect { it.message?.contains("dateOfBirth") == true }
            .verify()
    }
}
