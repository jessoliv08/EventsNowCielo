package com.example.eventsnowcielo.features.events.domain.usecase

import com.example.eventsnowcielo.features.events.domain.model.Category
import com.example.eventsnowcielo.features.events.domain.model.Event
import com.example.eventsnowcielo.features.events.domain.repository.EventRepository
import com.example.eventsnowcielo.features.events.ui.EventFilterState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.time.LocalDate

class GetEventsUseCaseImplTest {

    private val repository: EventRepository = mockk()
    private lateinit var useCase: GetEventsUseCaseImpl

    private val testToday = LocalDate.of(2026, 9, 18)

    private val pastEvent = Event(
        id = "1",
        title = "Past Music Festival",
        date = "2026-09-01",
        time = "18:00",
        location = "Venue A",
        priceInCents = 5000L,
        imageUrl = "http://example.com/1.jpg",
        description = "",
        category = Category("id_cat_01", "Show")
    )

    private val todayEvent = Event(
        id = "2",
        title = "Today Conference",
        date = "2026-09-18",
        time = "10:00",
        location = "Venue B",
        priceInCents = 10000L,
        imageUrl = "http://example.com/2.jpg",
        description = "",
        category = Category("id_cat_01", "Show")
    )

    private val futureEvent = Event(
        id = "3",
        title = "Future Concert",
        date = "2026-10-05",
        time = "21:00",
        location = "Venue C",
        priceInCents = 7500L,
        imageUrl = "http://example.com/3.jpg",
        description = "",
        category = Category("id_cat_01", "Show")
    )

    private val musicCat = Category(id = "cat_music", name = "Music")
    private val techCat = Category(id = "cat_tech", name = "Technology")

    private val rockConcert = Event(
        id = "1",
        title = "Rock Fest",
        imageUrl = "",
        date = "2026-10-10",
        time = "20:00",
        priceInCents = 15000L,
        description = "Live rock music",
        location = "Allianz Parque",
        category = musicCat
    )

    private val techConf = Event(
        id = "2",
        title = "Dev Summit",
        imageUrl = "",
        date = "2026-11-15",
        time = "09:00",
        priceInCents = 5000L,
        description = "Software development summit",
        location = "Expo Center Norte",
        category = techCat
    )

    private val indieShow = Event(
        id = "3",
        title = "Indie Night",
        imageUrl = "",
        date = "2026-12-01",
        time = "22:00",
        priceInCents = 2000L,
        description = "Acoustic sessions",
        location = "Bar da Rock",
        category = musicCat
    )

    private val allEventsTimeFilter = listOf(pastEvent, todayEvent, futureEvent)
    private val allEventsFilter = listOf(rockConcert, techConf, indieShow)

    @Before
    fun setUp() {
        useCase = GetEventsUseCaseImpl(repository)
    }

    @Test
    fun `invoke should return only today and future events when repository succeeds`() = runTest {
        // Given
        coEvery { repository.getEvents() } returns Result.success(allEventsTimeFilter)

        // When
        val result = useCase(today = testToday)

        // Then
        assertTrue(result.isSuccess)
        val filteredEvents = result.getOrNull()
        assertEquals(2, filteredEvents?.size)
        assertEquals(listOf(todayEvent, futureEvent), filteredEvents)
        coVerify(exactly = 1) { repository.getEvents() }
    }

    @Test
    fun `getPastEvents should return only past events when repository succeeds`() = runTest {
        // Given
        coEvery { repository.getEvents() } returns Result.success(allEventsTimeFilter)

        // When
        val result = useCase.getPastEvents(today = testToday)

        // Then
        assertTrue(result.isSuccess)
        val filteredEvents = result.getOrNull()
        assertEquals(1, filteredEvents?.size)
        assertEquals(listOf(pastEvent), filteredEvents)
        coVerify(exactly = 1) { repository.getEvents() }
    }

    @Test
    fun `invoke should keep events with unparseable dates as fallback`() = runTest {
        // Given
        val malformedEvent = Event(
            id = "4",
            title = "Invalid Date Event",
            date = "invalid-date",
            time = "12:00",
            location = "Venue D",
            priceInCents = 2000L,
            imageUrl = "",
            description = "",
            category = Category("id_cat_01", "Show")
        )
        coEvery { repository.getEvents() } returns Result.success(listOf(malformedEvent))

        // When
        val result = useCase(today = testToday)

        // Then
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull()?.size)
        assertEquals(malformedEvent, result.getOrNull()?.first())
    }

    @Test
    fun `getPastEvents should exclude events with unparseable dates as fallback`() = runTest {
        // Given
        val malformedEvent = Event(
            id = "4",
            title = "Invalid Date Event",
            date = "invalid-date",
            time = "12:00",
            location = "Venue D",
            priceInCents = 2000L,
            imageUrl = "",
            description = "",
            category = Category("id_cat_01", "Show")
        )
        coEvery { repository.getEvents() } returns Result.success(listOf(malformedEvent))

        // When
        val result = useCase.getPastEvents(today = testToday)

        // Then
        assertTrue(result.isSuccess)
        assertTrue(result.getOrNull()?.isEmpty() == true)
    }

    @Test
    fun `invoke should return failure when repository fails`() = runTest {
        // Given
        val exception = RuntimeException("Database error")
        coEvery { repository.getEvents() } returns Result.failure(exception)

        // When
        val result = useCase(today = testToday)

        // Then
        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
        coVerify(exactly = 1) { repository.getEvents() }
    }

    @Test
    fun `getCategories should return unique sorted categories when repository succeeds`() = runTest {
        val sampleEvent = Event(
            id = "2",
            title = "Today Conference",
            date = "2026-09-18",
            time = "10:00",
            location = "Venue B",
            priceInCents = 10000L,
            imageUrl = "http://example.com/2.jpg",
            description = "",
            category = Category("id_cat_01", "Show")
        )
        // Given
        val musicCategory = Category(id = "cat-1", name = "Music")
        val sportsCategory = Category(id = "cat-2", name = "Sports")

        val event1 = sampleEvent.copy(id = "1", category = sportsCategory)
        val event2 = sampleEvent.copy(id = "2", category = musicCategory)
        val event3 = sampleEvent.copy(id = "3", category = musicCategory) // Duplicate category

        coEvery { repository.getEvents() } returns Result.success(listOf(event1, event2, event3))

        // When
        val result = useCase.getCategories()

        // Then
        assertTrue(result.isSuccess)
        val categories = result.getOrNull()
        assertEquals(2, categories?.size)
        assertEquals(listOf(musicCategory, sportsCategory), categories) // Alphabetically sorted
    }

    @Test
    fun `searchByNameOrLocation should return matching events by title or location case-insensitively`() = runTest {
        coEvery { repository.getEvents() } returns Result.success(allEventsFilter)

        // Search title match ("rock")
        val titleResult = useCase.searchByNameOrLocation("rock")
        assertTrue(titleResult.isSuccess)
        assertEquals(listOf(rockConcert, indieShow), titleResult.getOrNull())

        // Search location match ("expo")
        val locationResult = useCase.searchByNameOrLocation("EXPO")
        assertTrue(locationResult.isSuccess)
        assertEquals(listOf(techConf), locationResult.getOrNull())
    }

    @Test
    fun `searchByNameOrLocation should return all events when query is blank`() = runTest {
        coEvery { repository.getEvents() } returns Result.success(allEventsFilter)

        val result = useCase.searchByNameOrLocation("   ")

        assertTrue(result.isSuccess)
        assertEquals(allEventsFilter, result.getOrNull())
    }

    // --- Date Range Tests ---

    @Test
    fun `getByDateRange should return events within start and end date inclusive`() = runTest {
        coEvery { repository.getEvents() } returns Result.success(allEventsFilter)

        val startDate = LocalDate.of(2026, 10, 1)
        val endDate = LocalDate.of(2026, 11, 20)

        val result = useCase.getByDateRange(startDate, endDate)

        assertTrue(result.isSuccess)
        assertEquals(listOf(rockConcert, techConf), result.getOrNull())
    }

    // --- Price Range Tests ---

    @Test
    fun `getByPriceRange should filter events within min and max price inclusive`() = runTest {
        coEvery { repository.getEvents() } returns Result.success(allEventsFilter)

        val result = useCase.getByPriceRange(minPriceInCents = 2000L, maxPriceInCents = 10000L)

        assertTrue(result.isSuccess)
        assertEquals(listOf(techConf, indieShow), result.getOrNull())
    }

    // --- Category Filter Tests ---

    @Test
    fun `getByCategory should filter events by category ID case-insensitively`() = runTest {
        coEvery { repository.getEvents() } returns Result.success(allEventsFilter)

        val result = useCase.getByCategory("CAT_MUSIC")

        assertTrue(result.isSuccess)
        assertEquals(listOf(rockConcert, indieShow), result.getOrNull())
    }

    // --- applyFilters Integration Unit Tests ---

    @Test
    fun `applyFilters returns all upcoming events when filter state is default`() = runTest {
        coEvery { repository.getEvents() } returns Result.success(allEventsTimeFilter)

        val result = useCase.applyFilters(EventFilterState(), today = testToday)

        assertTrue(result.isSuccess)
        assertEquals(listOf(todayEvent, futureEvent), result.getOrNull())
    }

    @Test
    fun `applyFilters fetches past events when isPastOnly is true`() = runTest {
        coEvery { repository.getEvents() } returns Result.success(allEventsTimeFilter)

        val filterState = EventFilterState(isPastOnly = true)
        val result = useCase.applyFilters(filterState, today = testToday)

        assertTrue(result.isSuccess)
        assertEquals(listOf(pastEvent), result.getOrNull())
    }

    @Test
    fun `applyFilters correctly chains query category price and date constraints`() = runTest {
        coEvery { repository.getEvents() } returns Result.success(allEventsFilter)

        val filterState = EventFilterState(
            query = "Dev",
            selectedCategoryId = "cat_tech",
            minPrice = 0f,
            maxPrice = 100f, // $100 max = 10000 cents (techConf is 5000 cents)
            startDate = LocalDate.of(2026, 11, 1),
            endDate = LocalDate.of(2026, 11, 30)
        )

        val result = useCase.applyFilters(filterState, today = testToday)

        assertTrue(result.isSuccess)
        assertEquals(listOf(techConf), result.getOrNull())
    }
}