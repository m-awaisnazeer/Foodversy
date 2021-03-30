package com.communisolve.foodversy.ui.fooddetail.comment

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.communisolve.foodversy.R
import com.communisolve.foodversy.adapter.MyCommentsListAdapter
import com.communisolve.foodversy.callbacks.ICommentCallbackListner
import com.communisolve.foodversy.common.Common
import com.communisolve.foodversy.model.CommentModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import dmax.dialog.SpotsDialog

class CommentsFragment : BottomSheetDialogFragment(), ICommentCallbackListner {
    private lateinit var recycler_comment: RecyclerView
    private val viewModel: CommentsViewModel by viewModels()

    private var iCommentCallbackListner: ICommentCallbackListner
    private var dialog: AlertDialog? = null

    init {
        iCommentCallbackListner = this
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val itemView = LayoutInflater.from(context)
            .inflate(R.layout.bottom_sheet_comment_fragment, container, false)
        initViews(itemView)
        loadCommentFromFirebase()

        viewModel.mutableLiveDataCommentList.observe(viewLifecycleOwner, Observer {
            recycler_comment.adapter = MyCommentsListAdapter(requireContext(), it)
        })
        return itemView
    }

    private fun loadCommentFromFirebase() {
        dialog!!.show()
        val commentModels = ArrayList<CommentModel>()
        FirebaseDatabase.getInstance().getReference(Common.COMMENT_REF)
            .child(Common.foodSelected!!.id)
            .orderByChild("commentTimeStamp")
            .limitToLast(100)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (commentSnapshot in snapshot.children) {
                        val commentModel = commentSnapshot.getValue(CommentModel::class.java)
                        commentModels.add(commentModel!!)
                    }
                    iCommentCallbackListner.onCommentLoadSuccess(commentModels)
                }

                override fun onCancelled(error: DatabaseError) {
                    iCommentCallbackListner.onCommentLoadFailed(error.message)
                }

            })

    }

    private fun initViews(itemView: View?) {
        dialog = SpotsDialog.Builder().setContext(requireContext()).setCancelable(false).build()
        var linearLayoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, true)
        recycler_comment = itemView!!.findViewById(R.id.recycler_comment)
        recycler_comment.apply {
            setHasFixedSize(true)
            layoutManager = linearLayoutManager
            addItemDecoration(DividerItemDecoration(context, linearLayoutManager.orientation))
        }
    }

    override fun onCommentLoadSuccess(commentsList: List<CommentModel>) {
        dialog!!.dismiss()
        viewModel.setCommentList(commentsList)
    }

    override fun onCommentLoadFailed(message: String) {
        Toast.makeText(requireContext(), "${message}", Toast.LENGTH_SHORT).show()
    }

    companion object {
        private var instance: CommentsFragment? = null

        fun getInstance(): CommentsFragment {
            if (instance == null)
                instance = CommentsFragment()
            return instance!!
        }
    }

}