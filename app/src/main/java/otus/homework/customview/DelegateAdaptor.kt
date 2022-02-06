package otus.homework.customview

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlin.reflect.KClass

interface IDataDelegator {
    fun getItemCount(): Int
    fun getViewType(position: Int): Int
    fun getHolderCreator(viewType: Int): IHoldarCreator


    interface IHoldarCreator {
        fun createViewHolder(parent: ViewGroup): DelegateAdaptor.Holdar
    }
}

class DataDelegator(
    private var itemCount: Int,
    private var listViewTypes: List<Int>,
    private var typeToHolderCreatorI: Map<Int, IDataDelegator.IHoldarCreator>,
) : IDataDelegator {
    override fun getItemCount(): Int {
        return itemCount
    }

    override fun getViewType(position: Int): Int {
        return listViewTypes[position]
    }

    override fun getHolderCreator(viewType: Int): IDataDelegator.IHoldarCreator {
        return typeToHolderCreatorI[viewType]!!
    }
}

class HoldarCreator(val lambda: (parent: ViewGroup) -> DelegateAdaptor.Holdar) :
    IDataDelegator.IHoldarCreator {
    override fun createViewHolder(parent: ViewGroup): DelegateAdaptor.Holdar {
        return lambda(parent)
    }
}

class DelegateAdaptor(private val dataDelegator: IDataDelegator) :
    RecyclerView.Adapter<DelegateAdaptor.Holdar>() {

    abstract class Holdar(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holdar {
        return dataDelegator.getHolderCreator(viewType).createViewHolder(parent)
    }

    override fun onBindViewHolder(holder: Holdar, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return dataDelegator.getItemCount()
    }

    override fun getItemViewType(position: Int): Int {
        return dataDelegator.getViewType(position)
    }
}

inline fun <reified T : Any> T.castAndPutInto(
    position: Int,
    any: Any,
    maps: MutableMap<Int, T>,
) {
    val t: T = any as T
    maps[position] = t
}

fun fastHolderBuilder(
    itemView: View,
    binder: (position: Int) -> Unit,
): DelegateAdaptor.Holdar {
    return object : DelegateAdaptor.Holdar(itemView) {
        override fun bind(position: Int) {
            return binder(position)
        }
    }
}

inline fun <reified T : Any> MutableMap<Int, T>.putInto(mutableMap: MutableMap<KClass<*>, MutableMap<Int, out Any>>): MutableMap<Int, T> {
    val class_ = T::class
    mutableMap[class_] = this
    return this
}

inline fun <reified T : Any> KClass<T>.createIntToTypeMap(): MutableMap<Int, T> {
    return mutableMapOf()
}

class DelegateAdaptorBuildor {
    interface CellProvider<T : Any> {
        fun getItem(position: Int): T
        fun updateMapping(map: MutableMap<Int, T>)
        fun getMapping(): MutableMap<Int, T>
    }

    fun setNewCells(
        cells: List<Any>,
        initializationBlock: DelegateAdaptorCellsPreparorBlock.() -> Unit,
    ) {
        // run diff utils if there were no cells before?
        if (this.cells == null) {
            this.cells = cells
            val block = DelegateAdaptorCellsPreparorBlock(cells)
            block.initializationBlock()
            // fill cell providers with data
            for ((index, value) in cells.withIndex()) {
                val positionToCellsMap = currentCellsMapping[value::class] ?: continue

                @Suppress("UNCHECKED_CAST")
                value::class.castAndPutInto(index,
                    value,
                    positionToCellsMap.getMapping() as MutableMap<Int, Any>)
            }
        }
    }

    fun buildDelegateAdaptor(): DelegateAdaptor {
        val altDataDelegator = DataDelegator(
            cells!!.size,
            listViewTypes = getCellViewTypes(),
            typeToHolderCreatorI = getViewTypeToHolderMapping()
        )
        return DelegateAdaptor(altDataDelegator)
    }

    private var cells: List<Any>? = null
    var currentCellsMapping: MutableMap<KClass<*>, CellProvider<*>> = mutableMapOf()
    var klassToViewType: MutableMap<KClass<*>, Int> = mutableMapOf()
    var viewTypeToHoldar: MutableMap<Int, HoldarCreator> = mutableMapOf()
    var cellTypes: MutableList<Int> = mutableListOf()

    private fun getCellViewTypes(): List<Int> {
        val cells = cells ?: return emptyList()
        cellTypes.clear()
        cellTypes.addAll(cells.map { klassToViewType[it::class] ?: Int.MAX_VALUE })
        return cellTypes
    }

    private fun getViewTypeToHolderMapping(): MutableMap<Int, HoldarCreator> {
        return viewTypeToHoldar
    }

    inner class DelegateAdaptorCellsPreparorBlock(val cells: List<Any>) {

        inline fun <reified T : Any> registerTypeAndObtainProvider(klass: KClass<T>): CellProvider<T> {
            val oldUncastedCellProvider = currentCellsMapping[klass]
            val provider = if (oldUncastedCellProvider != null) {
                @Suppress("UNCHECKED_CAST")
                val oldCellProvider: CellProvider<T> = currentCellsMapping[klass] as CellProvider<T>
                oldCellProvider.updateMapping(T::class.createIntToTypeMap())
                oldCellProvider
            } else {
                object : CellProvider<T> {
                    val map = mutableMapOf<Int, T>()
                    override fun getItem(position: Int): T {
                        return map[position]!!
                    }

                    override fun updateMapping(map: MutableMap<Int, T>) {
                        this.map.clear()
                        this.map.putAll(map)
                    }

                    override fun getMapping(): MutableMap<Int, T> {
                        return map
                    }
                }.also {
                    currentCellsMapping[klass] = it
                    klassToViewType[klass] =
                        if (klassToViewType.isNotEmpty()) klassToViewType.values.maxOf { it } + 1 else 0
                }
            }

            return provider
        }

        inline fun <reified T : Any> registerAndObtainHolder(
            kClass: KClass<T>,
            cellProvider: CellProvider<T>,
            holdarCreator: (CellProvider<T>) -> HoldarCreator,
        ): HoldarCreator {
            val viewType = klassToViewType[kClass]
                ?: throw IllegalStateException("by moment of creating view holder view type must be registered for class, $kClass")
            return viewTypeToHoldar[viewType]
                ?: holdarCreator(cellProvider).also { viewTypeToHoldar[viewType] = it }
        }

        inline fun <reified T : Any> registerTypeAndHoldar(
            kClass: KClass<T>,
            holdarCreator: (CellProvider<T>) -> HoldarCreator,
        ) {
            val provider = registerTypeAndObtainProvider(kClass)
            registerAndObtainHolder(kClass, provider, holdarCreator)
        }

        inline fun <reified T : Any> registerTypeAndHoldarCreator(
            kClass: KClass<T>,
            crossinline holdarCreatorLambda: (viewGroup: ViewGroup, CellProvider<T>) -> DelegateAdaptor.Holdar,
        ) {

            val provider = registerTypeAndObtainProvider(kClass)
            val holdarCreationLambda = { cellProvider : CellProvider<T> ->
                HoldarCreator { parent ->
                    holdarCreatorLambda(parent, provider)
                }
            }
            registerAndObtainHolder(kClass, provider, holdarCreationLambda)
        }

    }
}
