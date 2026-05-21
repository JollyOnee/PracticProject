package org.infa252.project

/**
 * Кроссплатформенная реализация отсортированного словаря на основе Красно-черного дерева.
 * Обеспечивает O(log n) на поиск, вставку и удаление.
 */
class KmpTreeMap<K : Comparable<K>, V> : Iterable<Map.Entry<K, V>> {

    private enum class Color { RED, BLACK }

    private inner class Node(
        val key: K,
        var value: V,
        var color: Color = Color.RED,
        var left: Node? = null,
        var right: Node? = null,
        var parent: Node? = null
    )

    private var root: Node? = null
    private var _size: Int = 0
    val size: Int get() = _size

    /**
     * Вставляет значение по ключу. Если ключ уже существует, обновляет значение.
     */
    fun put(key: K, value: V) {
        var t = root
        if (t == null) {
            root = Node(key, value, Color.BLACK)
            _size = 1
            return
        }

        var parentNode: Node
        var cmp: Int
        do {
            parentNode = t!!
            cmp = key.compareTo(t.key)
            if (cmp < 0) t = t.left
            else if (cmp > 0) t = t.right
            else {
                t.value = value
                return
            }
        } while (t != null)

        val e = Node(key, value, Color.RED, parent = parentNode)
        if (cmp < 0) parentNode.left = e
        else parentNode.right = e

        fixAfterInsertion(e)
        _size++
    }

    /**
     * Возвращает значение по ключу или null, если ключ не найден.
     */
    fun get(key: K): V? {
        var current = root
        while (current != null) {
            val cmp = key.compareTo(current.key)
            if (cmp == 0) return current.value
            current = if (cmp < 0) current.left else current.right
        }
        return null
    }

    private fun colorOf(p: Node?): Color = p?.color ?: Color.BLACK
    private fun parentOf(p: Node?): Node? = p?.parent
    private fun leftOf(p: Node?): Node? = p?.left
    private fun rightOf(p: Node?): Node? = p?.right
    private fun setColor(p: Node?, c: Color) { p?.color = c }

    private fun rotateLeft(p: Node?) {
        if (p != null) {
            val r = p.right
            p.right = r?.left
            if (r?.left != null) r.left!!.parent = p
            r?.parent = p.parent
            if (p.parent == null) root = r
            else if (p.parent!!.left == p) p.parent!!.left = r
            else p.parent!!.right = r
            r?.left = p
            p.parent = r
        }
    }

    private fun rotateRight(p: Node?) {
        if (p != null) {
            val l = p.left
            p.left = l?.right
            if (l?.right != null) l.right!!.parent = p
            l?.parent = p.parent
            if (p.parent == null) root = l
            else if (p.parent!!.right == p) p.parent!!.right = l
            else p.parent!!.left = l
            l?.right = p
            p.parent = l
        }
    }

    private fun fixAfterInsertion(x: Node?) {
        var node = x
        node?.color = Color.RED

        while (node != null && node != root && node.parent?.color == Color.RED) {
            if (parentOf(node) == leftOf(parentOf(parentOf(node)))) {
                val y = rightOf(parentOf(parentOf(node)))
                if (colorOf(y) == Color.RED) {
                    setColor(parentOf(node), Color.BLACK)
                    setColor(y, Color.BLACK)
                    setColor(parentOf(parentOf(node)), Color.RED)
                    node = parentOf(parentOf(node))
                } else {
                    if (node == rightOf(parentOf(node))) {
                        node = parentOf(node)
                        rotateLeft(node)
                    }
                    setColor(parentOf(node), Color.BLACK)
                    setColor(parentOf(parentOf(node)), Color.RED)
                    rotateRight(parentOf(parentOf(node)))
                }
            } else {
                val y = leftOf(parentOf(parentOf(node)))
                if (colorOf(y) == Color.RED) {
                    setColor(parentOf(node), Color.BLACK)
                    setColor(y, Color.BLACK)
                    setColor(parentOf(parentOf(node)), Color.RED)
                    node = parentOf(parentOf(node))
                } else {
                    if (node == leftOf(parentOf(node))) {
                        node = parentOf(node)
                        rotateRight(node)
                    }
                    setColor(parentOf(node), Color.BLACK)
                    setColor(parentOf(parentOf(node)), Color.RED)
                    rotateLeft(parentOf(parentOf(node)))
                }
            }
        }
        root?.color = Color.BLACK
    }

    /**
     * Возвращает итератор для обхода дерева в порядке возрастания ключей (In-order traversal).
     */
    override fun iterator(): Iterator<Map.Entry<K, V>> {
        return object : Iterator<Map.Entry<K, V>> {
            private val stack = mutableListOf<Node>()
            private var current: Node? = root

            override fun hasNext(): Boolean = stack.isNotEmpty() || current != null

            override fun next(): Map.Entry<K, V> {
                while (current != null) {
                    stack.add(current!!)
                    current = current?.left
                }
                val node = stack.removeAt(stack.size - 1)
                val result = object : Map.Entry<K, V> {
                    override val key: K = node.key
                    override val value: V = node.value
                }
                current = node.right
                return result
            }
        }
    }

    /**
     * Возвращает все элементы в виде списка, отсортированного по ключу.
     */
    fun toList(): List<Map.Entry<K, V>> {
        val result = mutableListOf<Map.Entry<K, V>>()
        for (entry in this) result.add(entry)
        return result
    }

    fun clear() {
        root = null
        _size = 0
    }
}